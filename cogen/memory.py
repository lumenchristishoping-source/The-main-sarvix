import sqlite3
import json
import os
from datetime import datetime
from importance import calculate_importance
from topics import detect_topics
from emotions import detect_emotion

DB_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "memory.db")

def _get_conn():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA journal_mode=WAL")
    return conn

def _init_db():
    with _get_conn() as conn:
        conn.execute("""
            CREATE TABLE IF NOT EXISTS messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                role TEXT NOT NULL,
                content TEXT NOT NULL,
                importance INTEGER DEFAULT 1,
                emotion TEXT DEFAULT 'neutral',
                topics TEXT DEFAULT '[]',
                timestamp TEXT NOT NULL,
                conflict INTEGER DEFAULT 0,
                belief_score REAL DEFAULT 1.0
            )
        """)
        conn.execute("""
            CREATE TABLE IF NOT EXISTS repos (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                url       TEXT NOT NULL UNIQUE,
                owner     TEXT NOT NULL,
                repo_name TEXT NOT NULL,
                summary   TEXT,
                files     TEXT DEFAULT '[]',
                readme    TEXT,
                timestamp TEXT NOT NULL
            )
        """)
        conn.execute("""
            CREATE TABLE IF NOT EXISTS repos (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                url       TEXT NOT NULL UNIQUE,
                owner     TEXT NOT NULL,
                repo_name TEXT NOT NULL,
                summary   TEXT,
                files     TEXT DEFAULT '[]',
                readme    TEXT,
                timestamp TEXT NOT NULL
            )
        """)
        conn.commit()

_init_db()

def load_memory():
    with _get_conn() as conn:
        rows = conn.execute("SELECT * FROM messages ORDER BY id ASC").fetchall()
    return [dict(row) for row in rows]

def save_message(role, content):
    # Import inside to avoid circular imports
    from conflict import detect_conflict
    from belief import calculate_belief_score
    
    # Load existing memory
    memory = load_memory()
    
    # Calculate all the metrics
    importance = calculate_importance(content, memory_history=memory)
    topics = detect_topics(content)
    emotion = detect_emotion(content)
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    # Calculate conflict and belief score
    is_conflict, conflicting_entry = detect_conflict(content, memory)
    belief_score = calculate_belief_score(content, memory)
    
    # Save to database
    with _get_conn() as conn:
        conn.execute(
            """INSERT INTO messages 
               (role, content, importance, emotion, topics, timestamp, conflict, belief_score)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
            (role, content, importance, emotion, 
             json.dumps(topics), timestamp, 
             1 if is_conflict else 0, belief_score)
        )
        conn.commit()
    
    # Return the belief score for debugging
    return belief_score

def maybe_compress():
    """Compress memory if threshold reached"""
    try:
        from compression import should_compress, compress_memory
        memory = load_memory()
        if should_compress(memory):
            with _get_conn() as conn:
                compress_memory(memory, conn)
    except Exception as e:
        pass

def clear_memory():
    with _get_conn() as conn:
        conn.execute("DELETE FROM messages")
        conn.commit()

def get_memory_stats():
    mem = load_memory()
    if not mem:
        return "No memories"
    
    conflicts = sum(1 for m in mem if m.get('conflict', 0))
    avg_belief = sum(m.get('belief_score', 1.0) for m in mem) / len(mem)
    
    return f"Total: {len(mem)}, Conflicts: {conflicts}, Avg Belief: {avg_belief:.2f}"


def save_repo(url, owner, repo_name, summary, files, readme):
    """Save repo details to persistent memory"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with _get_conn() as conn:
        conn.execute("""
            INSERT OR REPLACE INTO repos
            (url, owner, repo_name, summary, files, readme, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """, (url, owner, repo_name, summary, json.dumps(files), readme, timestamp))
        conn.commit()


def get_repo(url):
    """Get saved repo details from memory"""
    with _get_conn() as conn:
        row = conn.execute(
            "SELECT * FROM repos WHERE url = ?", (url,)
        ).fetchone()
    if row:
        d = dict(row)
        d["files"] = json.loads(d.get("files") or "[]")
        return d
    return None

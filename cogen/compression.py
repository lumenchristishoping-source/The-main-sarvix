import json
from datetime import datetime

COMPRESSION_THRESHOLD = 20
KEEP_RECENT = 5

def should_compress(memory):
    user_messages = [m for m in memory if m["role"] == "user"]
    return len(user_messages) >= COMPRESSION_THRESHOLD

def compress_memory(memory, conn):
    """Compress oldest messages into a summary entry"""
    # Keep recent messages untouched
    to_compress = memory[:-KEEP_RECENT]
    to_keep = memory[-KEEP_RECENT:]

    if not to_compress:
        return

    # Build compression summary
    topics_all = []
    emotions_all = []
    important = []

    for msg in to_compress:
        topics = msg.get("topics", [])
        if isinstance(topics, str):
            try:
                topics = json.loads(topics)
            except:
                topics = []
        topics_all.extend(topics)
        emotions_all.append(msg.get("emotion", "neutral"))
        if msg.get("importance", 1) >= 6:
            important.append(msg.get("content", "")[:100])

    # Most common topics
    from collections import Counter
    top_topics = [t for t, _ in Counter(topics_all).most_common(5)]
    dominant_emotion = Counter(emotions_all).most_common(1)[0][0] if emotions_all else "neutral"

    summary_text = f"[COMPRESSED {len(to_compress)} messages] "
    summary_text += f"Topics: {', '.join(top_topics)}. "
    summary_text += f"Dominant emotion: {dominant_emotion}. "
    if important:
        summary_text += f"Key moments: " + " | ".join(important[:3])

    # Delete compressed messages from DB
    ids_to_delete = [m["id"] for m in to_compress if "id" in m]
    if ids_to_delete:
        placeholders = ",".join("?" * len(ids_to_delete))
        conn.execute(f"DELETE FROM messages WHERE id IN ({placeholders})", ids_to_delete)

    # Insert compressed summary
    conn.execute("""
        INSERT INTO messages (role, content, importance, emotion, topics, timestamp, conflict, belief_score)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """, (
        "system",
        summary_text,
        5,
        dominant_emotion,
        json.dumps(top_topics),
        datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        0,
        1.0
    ))
    conn.commit()
    print(f"[cogen] Compressed {len(to_compress)} messages into 1 summary")

import os

INSTALL_DIR = "/data/data/com.termux/files/usr/lib/cogen"

SELF_TRIGGERS = [
    "what are you made of",
    "show me your files",
    "explain yourself",
    "what are your files",
    "how do you work",
    "show me your source",
    "what is your source",
    "show your internals",
    "show me your internal",
    "what modules do you have",
    "show me yourself",
    "who made you",
    "what are you",
    "explain how you work",
]

FILE_DESCRIPTIONS = {
    "cogen.py": "Main CLI entry point — handles all commands and flags",
    "memory.py": "SQLite storage engine — saves and loads all messages and repos",
    "retrieval.py": "Context scoring — finds the most relevant memories for each message",
    "emotions.py": "Emotion detection — tags each message with an emotional state",
    "importance.py": "Importance scoring — rates each message from 1 to 10",
    "topics.py": "Topic detection — identifies what each message is about",
    "behaviour.py": "Behaviour analysis — detects patterns over time",
    "belief.py": "Belief scoring — handles contradictions and rates truth confidence",
    "conflict.py": "Contradiction detection — flags when new messages conflict with memory",
    "summaries.py": "Summary generation — creates continuity summaries from memory",
    "patterns.py": "Pattern analysis — finds recurring topics and emotional cycles",
    "ai.py": "AI caller — sends context and memory to the language model",
    "search.py": "Web search — fetches real time results via Tavily or DuckDuckGo",
    "search_triggers.py": "Search triggers — detects when a web search is needed",
    "repo.py": "Repo reader — fetches and reads GitHub repositories",
    "self.py": "Self awareness — allows Cogen to read and understand its own files",
    "tools.py": "MCP tools — exposes memory functions to external AI agents",
    "main.py": "Debug mode — runs Cogen with full context display",
}

def is_self_query(text):
    text_lower = text.lower()
    return any(trigger in text_lower for trigger in SELF_TRIGGERS)

def get_self_context():
    """Read own files and return self description"""
    files = []
    total_lines = 0

    if os.path.exists(INSTALL_DIR):
        for filename in sorted(os.listdir(INSTALL_DIR)):
            if filename.endswith('.py'):
                filepath = os.path.join(INSTALL_DIR, filename)
                try:
                    with open(filepath, 'r') as f:
                        lines = f.readlines()
                    line_count = len(lines)
                    total_lines += line_count
                    desc = FILE_DESCRIPTIONS.get(filename, "Module")
                    files.append(f"{filename} ({line_count} lines) — {desc}")
                except:
                    files.append(f"{filename} — (unreadable)")

    result = f"SELF CONTEXT — Cogen v3.1 installed at {INSTALL_DIR}\n"
    result += f"Total: {len(files)} modules, ~{total_lines} lines of code\n\n"
    result += "MODULES:\n" + "\n".join(files)
    return result

def read_own_file(filename):
    """Read a specific own file"""
    filepath = os.path.join(INSTALL_DIR, filename)
    if os.path.exists(filepath):
        with open(filepath, 'r') as f:
            return f.read()
    return None

def modify_own_file(filename, new_content):
    """Rewrite a specific own file"""
    filepath = os.path.join(INSTALL_DIR, filename)
    if os.path.exists(filepath):
        # Backup first
        backup = filepath + '.bak'
        with open(filepath, 'r') as f:
            original = f.read()
        with open(backup, 'w') as f:
            f.write(original)
        # Write new content
        with open(filepath, 'w') as f:
            f.write(new_content)
        return True
    return False

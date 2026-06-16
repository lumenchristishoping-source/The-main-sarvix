import os
os.environ["PYTHONUNBUFFERED"] = "1"

import sys, json

sys.path.append(os.path.dirname(os.path.abspath(__file__)))

import memory
import retrieval
import summaries

# Use main cogen database
memory.DB_PATH = "/sdcard/cogen/memory.db"

def rpc(_id, result):
    msg = {"jsonrpc": "2.0", "id": _id, "result": result}
    line = json.dumps(msg)
    sys.stdout.write(line + "\n")
    sys.stdout.flush()

def handle_tool(name, args):
    if name == "memory_retrieve":
        query = args.get("query", "")
        from topics import detect_topics
        from emotions import detect_emotion
        topics = detect_topics(query)
        emotion = detect_emotion(query)
        context = retrieval.get_context(
            current_topics=topics,
            current_emotion=emotion,
            raw_input=query
        )
        if not context:
            return "No relevant memories found."
        lines = [f"[{m['timestamp']}] {m['role'].upper()}: {m['content']}" for m in context]
        return "\n".join(lines)

    elif name == "memory_summary":
        mem = memory.load_memory()
        return summaries.generate_summary(mem)

    elif name == "memory_save":
        memory.save_message(args.get("role", "user"), args.get("content", ""))
        return "Saved to memory."

    elif name == "memory_clear":
        memory.clear_memory()
        return "Memory cleared."

    elif name == "repo_read":
        url = args.get("url", "")
        sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
        from repo import fetch_repo
        result = fetch_repo(url)
        return result if result else "Could not fetch repo."

    elif name == "web_search":
        query = args.get("query", "")
        sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
        from search import search_web
        results = search_web(query)
        if results:
            lines = []
            for r in results[:3]:
                lines.append(f"- {r['title']}: {r['content'][:200]}")
            return "\n".join(lines)
        return "No results found."

    return "Unknown tool."

TOOLS = [
    {
        "name": "memory_retrieve",
        "description": "Retrieve relevant memories based on a query",
        "inputSchema": {
            "type": "object",
            "properties": {
                "query": {"type": "string", "description": "Query to search memories"}
            },
            "required": ["query"]
        }
    },
    {
        "name": "memory_summary",
        "description": "Get a summary of behavioural patterns and memory",
        "inputSchema": {
            "type": "object",
            "properties": {}
        }
    },
    {
        "name": "memory_save",
        "description": "Save a message to memory",
        "inputSchema": {
            "type": "object",
            "properties": {
                "role": {"type": "string"},
                "content": {"type": "string"}
            },
            "required": ["role", "content"]
        }
    },
    {
        "name": "repo_read",
        "description": "Read a GitHub repository structure and README",
        "inputSchema": {
            "type": "object",
            "properties": {
                "url": {"type": "string", "description": "GitHub repo URL"}
            },
            "required": ["url"]
        }
    },
    {
        "name": "web_search",
        "description": "Search the web for current information",
        "inputSchema": {
            "type": "object",
            "properties": {
                "query": {"type": "string", "description": "Search query"}
            },
            "required": ["query"]
        }
    },
    {
        "name": "memory_clear",
        "description": "Clear all memory",
        "inputSchema": {
            "type": "object",
            "properties": {}
        }
    }
]

def handle(req):
    method = req.get("method", "")
    _id = req.get("id")
    params = req.get("params", {})

    if method == "initialize":
        rpc(_id, {
            "protocolVersion": "2024-11-05",
            "capabilities": {
                "tools": {}
            },
            "serverInfo": {
                "name": "cogen",
                "version": "3.1.0"
            }
        })

    elif method == "notifications/initialized":
        pass

    elif method == "tools/list":
        rpc(_id, {"tools": TOOLS})

    elif method == "tools/call":
        name = params.get("name", "")
        arguments = params.get("arguments", {})
        try:
            result = handle_tool(name, arguments)
            rpc(_id, {
                "content": [
                    {
                        "type": "text",
                        "text": str(result)
                    }
                ],
                "isError": False
            })
        except Exception as e:
            rpc(_id, {
                "content": [
                    {
                        "type": "text",
                        "text": f"Error: {str(e)}"
                    }
                ],
                "isError": True
            })

def main():
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        try:
            req = json.loads(line)
            handle(req)
        except Exception as e:
            sys.stdout.write(json.dumps({"error": str(e)}) + "\n")
            sys.stdout.flush()

if __name__ == "__main__":
    main()

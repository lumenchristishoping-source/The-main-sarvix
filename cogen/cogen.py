#!/usr/bin/env python3
"""
cogen — Continuity Engine CLI

Usage:
  cogen "your message"                     Single reply with memory
  cogen                                    Interactive session
  cogen --summary                          Show memory pattern summary
  cogen --clear                            Wipe all memory
  cogen --model openai/gpt-4o-mini "hi"   Use a specific model
  cogen --verbose "your message"           Show recalled context too
  cogen --help                             Show this help
"""

import sys
import os

# Works regardless of where cogen is called from
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, SCRIPT_DIR)

from memory import save_message, load_memory, clear_memory
from retrieval import get_context
from topics import detect_topics
from emotions import detect_emotion
from summaries import generate_summary
from ai import call_ai
from search import search_web
from search_triggers import should_search
from repo import fetch_repo, fetch_file, is_github_url, extract_github_url, extract_filepath
from self import is_self_query, get_self_context, read_own_file


# ── Colours ───────────────────────────────────────────────────────────────────
PURPLE = "\033[35m"
WHITE  = "\033[97m"
DIM    = "\033[90m"
RESET  = "\033[0m"
CLEAR  = " " * 20 + "\r"

def _p(colour, text): return f"{colour}{text}{RESET}"


# ── Display helpers ───────────────────────────────────────────────────────────

def print_header():
    memory    = load_memory()
    msg_count = len([m for m in memory if m["role"] == "user"])
    label     = f"{msg_count} past exchanges in memory" if msg_count else "fresh session — no memory yet"
    print(f"\n{_p(PURPLE, '● Continuity Engine')}  {_p(DIM, 'cogen v3.1')}")
    print(_p(DIM, "─" * 44))
    print(_p(DIM, label))
    print(_p(DIM, "Type 'exit' to quit · 'summary' to see patterns\n"))


def show_summary():
    memory  = load_memory()
    summary = generate_summary(memory)
    print(_p(DIM, "\n┌─ MEMORY SUMMARY " + "─" * 28))
    for line in summary.split("\n"):
        print(f"{_p(DIM, '│')}  {line.strip().lstrip('•').strip()}")
    print(_p(DIM, "└" + "─" * 46))


def show_context(context):
    if not context:
        return
    print(_p(DIM, "┌─ RECALLED " + "─" * 34))
    for msg in context:
        role    = msg["role"].upper()
        ts      = msg.get("timestamp", "")
        content = msg["content"]
        if len(content) > 72:
            content = content[:69] + "..."
        print(f"{_p(DIM, '│')} [{ts}] {_p(DIM, role + ':')} {content}")
    print(_p(DIM, "└" + "─" * 46))


# ── Core turn ─────────────────────────────────────────────────────────────────

def run_turn(user_input, model=None, verbose=False):
    topics  = detect_topics(user_input)
    emotion = detect_emotion(user_input)

    save_message("user", user_input)

    context = get_context(
        current_topics=topics,
        current_emotion=emotion,
        raw_input=user_input
    )

    memory  = load_memory()
    summary = generate_summary(memory)

    if verbose:
        show_context(context)

    # Self awareness check
    import re as _re
    file_match = _re.search(r'(?:show me your|read your|open your|your)\s+([\w]+\.py)', user_input.lower())
    if file_match:
        fname = file_match.group(1)
        file_content = read_own_file(fname)
        if file_content:
            summary = "YOUR FILE " + fname + ":\n" + file_content[:5000] + "\n\n" + summary
    elif is_self_query(user_input):
        self_context = get_self_context()
        summary = "SELF CONTEXT:\n" + self_context + "\n\n" + summary

    # Check for GitHub URL or repo-related question
    repo_url = extract_github_url(user_input)
    if not repo_url:
        repo_keywords = ["repo", "repository", "files", "made of", "source", "codebase"]
        if any(k in user_input.lower() for k in repo_keywords):
            try:
                import sqlite3
                conn = sqlite3.connect("/sdcard/cogen/memory.db")
                row = conn.execute("SELECT url FROM repos ORDER BY timestamp DESC LIMIT 1").fetchone()
                if row:
                    repo_url = row[0]
            except:
                pass

    if repo_url:
        if repo_url:
            filepath = extract_filepath(user_input)
            if filepath:
                file_content = fetch_file(repo_url, "continuity_engine/" + filepath)
                if not file_content:
                    file_content = fetch_file(repo_url, filepath)
                if file_content:
                    context.append({
                        "role": "system",
                        "content": "FILE CONTENT (" + filepath + ") FROM " + repo_url + ":\n" + file_content,
                        "importance": 10,
                        "emotion": "neutral",
                        "topics": ["coding"],
                        "timestamp": ""
                    })
            else:
                repo_text = fetch_repo(repo_url)
                if repo_text:
                    summary += "\n\nREPO CONTEXT:\n" + repo_text

    if should_search(user_input):
        try:
            results = search_web(user_input)
            if results:
                lines = []
                for r in results[:3]:
                    lines.append("- " + r["title"] + ": " + r["content"][:200])
                summary += "\n\nREPO CONTEXT:\n" + repo_text
        except Exception:
            pass

    try:
        response = call_ai(user_input, context, summary, preferred_model=model)
    except Exception as e:
        response = f"[Error: {e}]"

    save_message("assistant", response)
    return response


# ── Modes ─────────────────────────────────────────────────────────────────────

def show_session_stats(sc):
    try:
        from memory import load_memory, maybe_compress
        from compression import should_compress
        mem = load_memory()
        um = len([m for m in mem if m["role"] == "user"])
        if should_compress(mem):
            maybe_compress()
            print("\033[90mMemory compressed\033[0m")
        print("\033[90m" + "─"*44 + "\033[0m")
        print(f"\033[90mSession ended.\033[0m")
        print(f"\033[90mMessages this session : {sc}\033[0m")
        print(f"\033[90mTotal in memory       : {um}\033[0m")
        print("\033[90m" + "─"*44 + "\033[0m")
    except Exception as e:
        print("Session ended.")

def interactive_mode(model=None, verbose=False):
    print_header()
    sc = 0
    try:
        while True:
            try:
                user_input = input(f"{_p(WHITE, 'You:')} ").strip()
            except (KeyboardInterrupt, EOFError):
                print()
                show_session_stats(sc)
                break

            if not user_input:
                continue
            if user_input.lower() in ("exit", "quit", "q"):
                show_session_stats(sc)
                break
            if user_input.lower() == "summary":
                show_summary()
                continue

            print(f"{_p(DIM, 'Thinking...')}", end="\r")
            response = run_turn(user_input, model=model, verbose=verbose)
            print(CLEAR, end="")
            print(f"\n{_p(PURPLE, 'Cogen:')} {response}\n")
            sc += 1


    except KeyboardInterrupt:
        print()
        show_session_stats(sc)

def single_turn_mode(user_input, model=None, verbose=False):
    response = run_turn(user_input, model=model, verbose=verbose)
    print(response)


# ── Entry point ───────────────────────────────────────────────────────────────

def main():
    args = sys.argv[1:]
    model   = None
    verbose = False
    remaining = []

    i = 0
    while i < len(args):
        arg = args[i]
        if arg == "--model" and i + 1 < len(args):
            model = args[i + 1]
            i += 2
        elif arg in ("--verbose", "-v"):
            verbose = True
            i += 1
        elif arg == "--summary":
            show_summary()
            return
        elif arg == "--clear":
            clear_memory()
            print(_p(DIM, "Memory cleared."))
            return
        elif arg == "--search-engine":
            if i + 1 < len(args):
                engine = args[i + 1]
                if engine in ["tavily", "duckduckgo"]:
                    from search import set_engine
                    set_engine(engine)
                else:
                    print("Available engines: tavily, duckduckgo")
                i += 2
            else:
                from search import get_engine
                print(f"Current search engine: {get_engine()}")
                i += 1
            return
        elif arg in ("--help", "-h"):
            print(f"""[35m● Continuity Engine[0m  [90mcogen v3.1[0m
[90m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━[0m

[97mUSAGE[0m
  cogen                          Start interactive chat
  cogen "your message"           Single message with memory
  cogen-debug                    Debug mode — shows recalled context live

[97mFLAGS[0m
  --summary                      Show memory patterns and behaviour insights
  --clear                        Wipe all memory (fresh start)
  --verbose "message"            Show what memories were recalled
  --model "model-id" "message"   Use a specific AI model
  --help                         Show this help
  --search-engine [engine]       Switch search engine (tavily, duckduckgo)

[97mAVAILABLE MODELS[0m
  anthropic/claude-3-haiku-20240307     Claude 3 Haiku
  openai/gpt-4o-mini                    GPT-4o Mini
  mistralai/mistral-small-2603          Mistral Small
  mistralai/mistral-7b-instruct:free    Mistral 7B        [90m(free)[0m
  meta-llama/llama-3.1-8b-instruct:free Llama 3.1 8B      [90m(free)[0m
  google/gemma-3-12b-it:free            Gemma 3 12B       [90m(free)[0m
  deepseek/deepseek-r1:free             DeepSeek R1       [90m(free)[0m

[97mEXAMPLES[0m
  cogen                                        Start chatting
  cogen --model deepseek/deepseek-r1:free      Use DeepSeek free
  cogen --summary                              See your patterns
  cogen --clear                                Fresh start
  cogen --verbose "what do you know about me"  See recalled memories

[97mUNINSTALL[0m
  bash uninstall.sh              Remove cogen from your system

[90m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━[0m
[90mBuilt by Lumen · github.com/lumenchristishoping-source/continuity-engine[0m
""")
            return
        else:
            remaining.append(arg)
            i += 1

    if remaining:
        single_turn_mode(" ".join(remaining), model=model, verbose=verbose)
    else:
        interactive_mode(model=model, verbose=verbose)


if __name__ == "__main__":
    main()

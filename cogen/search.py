import requests
import os
import json

CONFIG_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "search_config.json")

TAVILY_API_KEY = os.getenv("TAVILY_API_KEY")

def get_engine():
    try:
        with open(CONFIG_PATH) as f:
            return json.load(f).get("engine", "tavily")
    except:
        return "tavily"

def set_engine(engine):
    with open(CONFIG_PATH, "w") as f:
        json.dump({"engine": engine}, f)
    print(f"Search engine set to: {engine}")

def search_tavily(query, max_results=5):
    response = requests.post(
        "https://api.tavily.com/search",
        json={
            "api_key": TAVILY_API_KEY,
            "query": query,
            "max_results": max_results
        },
        timeout=20
    )
    data = response.json()
    results = []
    for item in data.get("results", []):
        results.append({
            "title": item.get("title", ""),
            "content": item.get("content", ""),
            "url": item.get("url", "")
        })
    return results

def search_duckduckgo(query, max_results=5):
    response = requests.get(
        "https://api.duckduckgo.com/",
        params={"q": query, "format": "json", "no_html": 1},
        timeout=20
    )
    data = response.json()
    results = []
    # Abstract result
    if data.get("Abstract"):
        results.append({
            "title": data.get("Heading", "DuckDuckGo"),
            "content": data.get("Abstract", ""),
            "url": data.get("AbstractURL", "")
        })
    # Related topics
    for item in data.get("RelatedTopics", [])[:max_results]:
        if isinstance(item, dict) and item.get("Text"):
            results.append({
                "title": item.get("Text", "")[:50],
                "content": item.get("Text", ""),
                "url": item.get("FirstURL", "")
            })
    return results

def search_web(query, max_results=5):
    engine = get_engine()
    try:
        if engine == "duckduckgo":
            return search_duckduckgo(query, max_results)
        else:
            return search_tavily(query, max_results)
    except Exception as e:
        # Fallback to other engine
        try:
            if engine == "duckduckgo":
                return search_tavily(query, max_results)
            else:
                return search_duckduckgo(query, max_results)
        except:
            return []

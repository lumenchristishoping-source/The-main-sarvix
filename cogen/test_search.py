from search import search_web

results = search_web("latest OpenRouter news")

for r in results:
    print(r["title"])
    print(r["url"])
    print("-" * 50)

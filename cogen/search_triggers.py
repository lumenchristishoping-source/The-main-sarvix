def should_search(message):
    message = message.lower()

    triggers = [

        # Current information
        "today",
        "right now",
        "currently",
        "current",
        "latest",
        "new",
        "recent",
        "update",
        "updates",
        "breaking",
        "news",

        # Search requests
        "search",
        "look up",
        "find",
        "check",
        "research",
        "investigate",

        # Questions
        "who is",
        "what is",
        "where is",
        "when did",
        "when was",
        "why did",
        "how much",
        "how many",

        # Products
        "best",
        "review",
        "reviews",
        "worth it",
        "buy",
        "price",
        "pricing",
        "cost",

        # Companies
        "company",
        "startup",
        "website",
        "official site",

        # Technology
        "github",
        "repo",
        "repository",
        "release",
        "version",
        "documentation",

        # Finance
        "stock",
        "stocks",
        "crypto",
        "bitcoin",
        "ethereum",
        "market",

        # Sports
        "score",
        "match",
        "game",
        "league",
        "standings",

        # People
        "net worth",
        "age",
        "biography",

        # General
        "tell me about",
        "information on",
        "details about",
        "facts about"
    ]

    return any(trigger in message for trigger in triggers)

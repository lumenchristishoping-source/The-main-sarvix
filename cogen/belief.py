def calculate_belief_score(new_content, memory):
    """
    Returns a belief score 0.0–1.0.
    Starts at 1.0, drops to 0.3 on contradiction, boosted by past confirmations.
    """
    from conflict import detect_conflict

    new_lower = new_content.lower()
    score = 1.0

    is_conflict, _ = detect_conflict(new_content, memory)
    if is_conflict:
        score = 0.3

    confirmation_phrases = [
        "i like", "i love", "i enjoy", "i hate", "i dislike",
        "i am", "i'm", "i prefer", "i always", "i never"
    ]

    for phrase in confirmation_phrases:
        if phrase in new_lower:
            confirmations = sum(
                1 for msg in memory
                if phrase in msg.get("content", "").lower()
                and msg.get("belief_score", 1.0) >= 0.7
            )
            boost = min(confirmations * 0.1, 0.4)
            if not is_conflict:
                score = min(1.0, score + boost)
            break

    return round(score, 2)

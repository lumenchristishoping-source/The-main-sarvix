from memory import save_message, load_memory
from retrieval import get_context
from summaries import generate_summary
from ai import call_ai

print("Cogen Example Chat")
print("Type 'exit' to quit.\n")

while True:
    user_input = input("You: ")

    if user_input.lower() == "exit":
        break

    save_message("user", user_input)

    context = get_context(raw_input=user_input)
    summary = generate_summary(load_memory())

    response = call_ai(user_input, context, summary)

    save_message("assistant", response)

    print(f"\nAI: {response}\n")
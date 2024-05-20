import os
import anthropic
import re
from dotenv import load_dotenv
import subprocess

load_dotenv()
api_key = os.getenv("ANTHROPIC_API_KEY")

client = anthropic.Anthropic(api_key=api_key)

MAX_CALLS = 4


def extract_code_and_changes(response):
    code_pattern = r"```(.*?)```"
    code_match = re.search(code_pattern, response, re.DOTALL)

    if code_match:
        code = code_match.group(1).strip()

        code_lines = code.split("\n")
        if len(code_lines) > 0 and code_lines[0].strip().lower() == "java":
            code = "\n".join(code_lines[1:])

        changes_start = response.find("```", code_match.end()) + 3
        changes = response[changes_start:].strip()

        return code, changes
    else:
        return None, None


def test_code(original_cobol_file, output_file):
    check_result = subprocess.run(
        ["./check.sh", original_cobol_file, output_file],
        capture_output=True, text=True
    )
    check_output = check_result.stdout
    check_error = check_result.stderr

    print(f"OUTPUT CAPTURED: \n\n{check_output}")
    print(f"ERROR CAPTURED: \n\n{check_error}")

    return "The outputs are the same." in check_output, check_error


def write_to_files(file_path, improved_code, changes):
    output_file = os.path.join("output", os.path.basename(file_path))
    with open(output_file, "w") as file:
        file.write(improved_code)

    log_file = os.path.join(
        "output",
        f"changelog-{os.path.splitext(os.path.basename(file_path))[0]}.log"
    )
    with open(log_file, "a") as file:
        file.write(changes + "\n\n")

    return output_file


def process_file(file_path):
    with open(file_path, "r") as file:
        code = file.read()

    prompt_file = os.path.join(os.path.dirname(__file__), "prompt.txt")
    with open(prompt_file, "r") as file:
        prompt = file.read()

    conversation = [
        {"role": "user", "content": f"{prompt}\n\n```\n{code}\n```"}]

    call_count = 0

    while call_count < MAX_CALLS:
        response = client.messages.create(
            model="claude-3-opus-20240229",
            max_tokens=4000,
            temperature=0.0,
            system="You are a helpful assistant.",
            messages=conversation,
        )

        improved_code, changes = extract_code_and_changes(
            response.content[0].text)

        if improved_code is None:
            conversation.append(
                {"role": "assistant", "content": response.content[0].text}
            )
            conversation.append(
                {
                    "role": "user",
                    "content": "Please continue or finish writing the code.",
                }
            )
            continue

        output_file = write_to_files(file_path, improved_code, changes)

        original_cobol_file = os.path.join(
            "cbl",
            os.path.splitext(os.path.basename(file_path))[0] + ".cbl"
        )
        test_passed, check_error = test_code(
            original_cobol_file, output_file)

        if test_passed:
            break

        conversation.append(
            {"role": "assistant", "content": response.content[0].text})
        conversation.append({"role": "user", "content": f"{check_error}"})

        call_count += 1

    if not test_passed:
        print(
            "COULD NOT RESOLVE TASK IN THE GIVEN NUMBER OF CALLS. "
            "HUMAN ASSISTANCE REQUIRED"
        )


java_files = [file for file in os.listdir("java") if file.endswith(".java")]

for file in java_files:
    file_path = os.path.join("java", file)
    process_file(file_path)

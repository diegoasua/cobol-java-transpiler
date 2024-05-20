# Getting started

1. In the root directory run `poetry install`. If poetry is not installed in your system refer to [this guide](https://python-poetry.org/docs/)
2. This project uses Anthropic's Claude Opus LLM. You need an [Anthropic API](https://docs.anthropic.com/claude/docs/getting-access-to-claude) key which is placed in the `.env` file. You won't need an OpenAI API key.
3. To start the agent from the root directory run `poetry run main.py`

# Background

This solution aims to provide a PoC of how to use an AI agent to improve COBOL to Java transpiling.

Traditional static transpilers often generate Java code that, while functional, can be verbose, difficult to maintain, and challenging for even experienced Java developers to understand. The idea behind this approach is to introduce an additional layer that produces idiomatic, object-oriented Java code with well-defined classes and intuitive variable naming, rather than adhering to the programming styles commonly found in COBOL. This can help old industries relying on expensive IBM mainframe infrastructure update their codebases easily and move to more easily maintainable infra, that can run orders of magnitude cheaper in modern clouds free of extravagant royalties. It can also help alleviate the lack of COBOL programmers needed to provide basic maintenance of these systems. A lot of these industries are central to the economic engine of developed nations such as everyday payment processors and financial institutions with hundreds of billions of dollars worth of AUM.

# Some basics

This challenge can be tackled in various ways. But in order to write a simple but effective solution that works out of the box and can be developed quickly I decided for the following. Instead of trying to transpile COBOL code directly into Java from 0, it makes more sense to use existing transpilers and add an AI agent as a layer on top of that. After all, from what I can see the static transpiler produced files that work.

Given that I worked direcly on the `java/*.java` files and did not have to touch the COBOL scripts and `CPY` files other than for testing the output. At first glance those files are not very readible. They have obscure variable and function names like `_9999abendProgram()` and do not utilize many standard Java constructs and libraries.

The first test is to be able to see if an LLM can produce usable code from this. I iterated some prompts until I landed on `src/prompt.txt`. This was able to produce almost working code. It was clear however and from my experience working with LLMs that this taks would require several steps, so a chain was implemented where if the produced file does not pass the tests, the errors are fed back to the LLM for a revision. This can be done multiple times and improves the accuracy. The current solution is not a jack for all trades and can fail in many ways that will be discussed later, but does the job for this reduced exercise.

Another point to mention is the choice of LLM. For coding and transpiling and similar tasks, and in general for agentic frameworks one needs to use the strongest LLMs available. Among all the GPT models out there, as of now, Claude Opus is in my experience the one that:

1. Produces the most reliable code
2. Follows instructions most accurately
3. Extrapolates best, instead of regurgitating code (sometimes referred to as stocastic parrot)
4. Is least prone to laziness, which in the context of LLMs refers to the reluctance to output a complete code block and instead providing only indications.

Given that we won't be using OpenAI's API, but Anthropic's. Both have a very similar schema if not the same, so changing between them is trivial. We will use a temperature of 0. The temperature parameter in LLMs controls the statiscal variability in the output layer. Basically LLMs sample from a distribution of possible tokens (say in the order of around 50,000, depdending on the tokenizer) and the temperature determines how much variance is in the ranking of the next token. This is just a number in a [softmax equation](https://en.wikipedia.org/wiki/Softmax_function). In simple terms high temperature makes LLMs more creative, and lower temperature makes them more strict (less hallucinations). For coding and agentic tasks, it's best to use a temperature of 0 since we prioritize well-defined outputs over creativity.

After making a call to the LLM, the response is parsed to extract the code and the changes. The test is then run to compare the new files with the COBOL source, and this action is repeated until the tests pass or the maximum number of allowed iterations is reached. This process is performed independently for each Java file.

# Future directions and further improvements

This example demonstrates a basic approach. It's important to note that LLMs, in their current state, require supervision for complex tasks. Creating production apps that use LLMs for agentic and coding tasks with automatic flows remains challenging. In my opinion, LLMs currently function better as a copilot rather than as an independent system, unlike unit testing or deployment frameworks. I believe in the concept of "human-in-the-middle," where a human supervisor still handles challenges that LLMs struggle to solve.

This example showcases a simple chain. Moving forward, one can adopt more sophisticated prompting agentic frameworks like [REACT](https://www.promptingguide.ai/techniques/react).  Another advanced strategy would be to programmatically iterate prompts using [DSPy](https://github.com/stanfordnlp/dspy).

I have written this code without relying on glue libraries because I personally prefer lean environments. A more straightforward approach to quickly reach a demo would be to use [LangChain](https://www.langchain.com), which comes with batteries included.

In a more complex scenario, when refactoring one of the Java files, all of them might need to be considered. The input context window and output of existing LLMs are limited, and long contexts are penalized by high costs. An alternative approach could be to use RAG (Retrieval-Augmented Generation) with embeddings over the codebase and retrieve only the relevant files for refactoring the targeted file. For that we could use off-the-shelf embedding models like OpenAI's Ada and connectors, parsers and chunking strategies from [LlamaIndex](https://www.llamaindex.ai/).

In general this code would need a lot lot more stability to be used in any production system. One library I am recently exploring is [Instructor](https://github.com/jxnl/instructor). Think of it as `Pydantic` for LLMs. Or also `JSON` guaranteed modes that OpenAI and Anthropic provide out of the box. Basically the parsing of the LLM output into code is bad and dirty and by no means guarantees the output.
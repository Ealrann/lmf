package isotropy.lmf.core.resource.ptree;

import isotropy.lmf.core.resource.util.Tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

public final class PTreeReader
{
	private static final String QUOTED_REGEX = "(?=[\"'])(?:\"[^\"\\\\]*(?:\\\\[\\s\\S][^\"\\\\]*)*\"|'[^'\\\\]*(?:\\\\[\\s\\S][^'\\\\]*)*')";
	private static final String WORD_REGEX = "[^\\s()\"]+";
	private static final String PARENTHESIS_REGEX = "[()]";

	private static final String PARSE_REGEX = String.join("|", QUOTED_REGEX, WORD_REGEX, PARENTHESIS_REGEX);
	private static final Pattern PARSE_PATTERN = Pattern.compile(PARSE_REGEX);

	private ReaderBuildState buildState = null;

	public Tree<List<String>> read(final InputStream inputStream)
	{
		try
		{
			buildState = new ReaderBuildState();
			parseFile(inputStream);
			return buildState.buildTree();
		}
		finally
		{
			buildState = null;
		}
	}

	private void parseFile(final InputStream inputStream)
	{
		assert inputStream != null;
		try (final var inputStreamReader = new InputStreamReader(inputStream))
		{
			try (final var reader = new BufferedReader(inputStreamReader))
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					final var matcher = PARSE_PATTERN.matcher(line);
					while (matcher.find())
					{
						final var word = trimQuotes(matcher.group());
						parseWord(word);
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void parseWord(final String word)
	{
		if (word.equals("("))
		{
			buildState.stack();
		}
		else if (word.equals(")"))
		{
			buildState.pop();
		}
		else
		{
			buildState.addWord(word);
		}
	}

	private static String trimQuotes(final String word)
	{
		if (word.startsWith("\""))
		{
			return word.substring(1, word.length() - 1);
		}
		else
		{
			return word;
		}
	}

	private static final class ReaderBuildState
	{
		private final Deque<PTreeBuilder> stack = new ArrayDeque<>();
		private final PTreeBuilder tree = new PTreeBuilder();

		private boolean mergeWithPreviousWord = false;

		public Tree<List<String>> buildTree()
		{
			return tree.build();
		}

		public void stack()
		{
			assert !mergeWithPreviousWord;
			final var newNode = stack.isEmpty()
					? tree.newChild()
					: stack.getLast()
						   .newChild();
			stack.add(newNode);
		}

		public void pop()
		{
			assert !mergeWithPreviousWord;
			stack.removeLast();
		}

		public void addWord(final String word)
		{
			if (mergeWithPreviousWord)
			{
				stack.getLast()
					 .mergeWithLastWord(word);
				mergeWithPreviousWord = false;
			}
			else
			{
				stack.getLast()
					 .addWord(word);
				if (word.endsWith("="))
				{
					mergeWithPreviousWord = true;
				}
			}
		}
	}
}

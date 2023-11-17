package org.logoce.lmf.editor.util;

import com.intellij.lang.ASTNode;
import org.logoce.lmf.editor.format.LMGroupBlock;
import org.logoce.lmf.model.resource.util.SoftIterator;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ASTChildrenIterable implements Iterable<ASTNode>
{
	private final ASTNode parent;

	public ASTChildrenIterable(ASTNode parent)
	{
		this.parent = parent;
	}

	public Stream<ASTNode> streamChildren()
	{
		return StreamSupport.stream(spliterator(), false);
	}

	@Override
	public Iterator<ASTNode> iterator()
	{
		final var iterationContext = new ASTIterationContext(parent.getFirstChildNode());
		return new SoftIterator<>(iterationContext::next);
	}

	private static final class ASTIterationContext
	{
		private ASTNode current;

		private ASTIterationContext(ASTNode current)
		{
			this.current = current.getFirstChildNode();
		}

		public Optional<ASTNode> next()
		{
			if (current != null)
			{
				current = current.getTreeNext();
				return Optional.of(current);
			}
			else
			{
				return Optional.empty();
			}
		}
	}
}

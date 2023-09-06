package isotropy.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

public interface TypeParameter
{
	TypeName parametrized();

	TypeName parametrizedWildcard();

	static TypeParameter of(ClassName raw, TypeName param)
	{
		return new SimpleTypeParameter(raw, List.of(param.box()));
	}

	static TypeParameter of(ClassName raw, int genericCount)
	{
		return new SimpleTypeParameter(raw, GenUtils.wildcardList(genericCount));
	}

	static TypeParameter of(ClassName raw, List<? extends TypeName> params)
	{
		return new SimpleTypeParameter(raw, params.stream().map(TypeName::box).toList());
	}

	static TypeParameter of(TypeName raw)
	{
		return new SimpleType(raw);
	}

	default TypeParameter nestIn(ClassName raw)
	{
		return new CombinedTypeParameter(raw, this);
	}

	record SimpleType(TypeName raw) implements TypeParameter
	{
		@Override
		public TypeName parametrized()
		{
			return raw;
		}

		@Override
		public TypeName parametrizedWildcard()
		{
			return raw;
		}
	}

	record SimpleTypeParameter(ClassName raw, List<? extends TypeName> params) implements TypeParameter
	{
		@Override
		public TypeName parametrized()
		{
			return params.isEmpty() ? raw : ParameterizedTypeName.get(raw, params.toArray(new TypeName[0]));
		}

		@Override
		public TypeName parametrizedWildcard()
		{
			if (params.isEmpty())
			{
				return raw;
			}
			else
			{
				final var genericParams = GenUtils.wildcardArray(params.size());
				return ParameterizedTypeName.get(raw, genericParams);
			}
		}
	}

	record CombinedTypeParameter(ClassName raw, TypeParameter nested) implements TypeParameter
	{
		@Override
		public TypeName parametrized()
		{
			return ParameterizedTypeName.get(raw, nested.parametrized().box());
		}

		@Override
		public TypeName parametrizedWildcard()
		{
			return ParameterizedTypeName.get(raw, nested.parametrizedWildcard().box());
		}
	}
}

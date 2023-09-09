package isotropy.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

public interface TypeParameter
{
	ClassName raw();
	TypeName parametrized();
	TypeName parametrizedWildcard();
	List<? extends TypeName> parameters();

	static TypeParameter of(ClassName raw, TypeName param)
	{
		return new SimpleTypeParameter(raw, List.of(param.box()));
	}

	static TypeParameter of(ClassName raw)
	{
		return new SimpleTypeParameter(raw, List.of());
	}

	static TypeParameter of(ClassName raw, int genericCount)
	{
		return new SimpleTypeParameter(raw, GenUtils.wildcardList(genericCount));
	}

	static TypeParameter of(ClassName raw, List<? extends TypeName> params)
	{
		return new SimpleTypeParameter(raw, params.stream().map(TypeName::box).toList());
	}

	static TypeParameter ofPrimitive(TypeName raw)
	{
		return new SimpleType(raw);
	}

	default TypeParameter nestIn(ClassName raw)
	{
		return new CombinedTypeParameter(raw, this);
	}
	default TypeParameter nest(TypeName newParam)
	{
		return TypeParameter.of(raw(), newParam);
	}

	record SimpleType(TypeName rawType) implements TypeParameter
	{
		@Override
		public ClassName raw()
		{
			throw new IllegalStateException();
		}

		@Override
		public TypeName parametrized()
		{
			return rawType;
		}

		@Override
		public TypeName parametrizedWildcard()
		{
			return rawType;
		}

		@Override
		public List<? extends TypeName> parameters()
		{
			return List.of();
		}
	}

	record SimpleTypeParameter(ClassName raw, List<? extends TypeName> parameters) implements TypeParameter
	{
		@Override
		public TypeName parametrized()
		{
			return parameters.isEmpty() ? raw : ParameterizedTypeName.get(raw, parameters.toArray(new TypeName[0]));
		}

		@Override
		public TypeName parametrizedWildcard()
		{
			if (parameters.isEmpty())
			{
				return raw;
			}
			else
			{
				final var genericParams = GenUtils.wildcardArray(parameters.size());
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

		@Override
		public List<? extends TypeName> parameters()
		{
			return List.of(nested.parametrized());
		}
	}
}

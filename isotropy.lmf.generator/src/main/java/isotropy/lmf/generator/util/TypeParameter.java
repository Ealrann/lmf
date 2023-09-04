package isotropy.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.List;

public interface TypeParameter
{
	TypeName parametrized();

	TypeName parametrizedWildcard();

	static TypeParameter of(ClassName raw, TypeName... params)
	{
		return new SimpleTypeParameter(raw, params);
	}

	static TypeParameter of(ClassName raw, List<? extends TypeName> params)
	{
		return new SimpleTypeParameter(raw, params.toArray(new TypeName[0]));
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

	record SimpleTypeParameter(ClassName raw, TypeName... params) implements TypeParameter
	{
		@Override
		public TypeName parametrized()
		{
			return params.length == 0 ? raw : ParameterizedTypeName.get(raw, params);
		}

		@Override
		public TypeName parametrizedWildcard()
		{
			if (params.length == 0)
			{
				return raw;
			}
			else
			{
				final var genericParams = new ClassName[params.length];
				Arrays.fill(genericParams, ClassName.get("", "?"));
				return ParameterizedTypeName.get(raw, genericParams);
			}
		}
	}

	record CombinedTypeParameter(ClassName raw, TypeParameter nested) implements TypeParameter
	{
		@Override
		public TypeName parametrized()
		{
			return ParameterizedTypeName.get(raw,
											 nested.parametrized()
												   .box());
		}

		@Override
		public TypeName parametrizedWildcard()
		{
			return ParameterizedTypeName.get(raw,
											 nested.parametrizedWildcard()
												   .box());
		}
	}
}

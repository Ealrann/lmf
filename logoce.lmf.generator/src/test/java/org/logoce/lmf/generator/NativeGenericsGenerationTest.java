package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class NativeGenericsGenerationTest
{
	@Test
	public void generateGenericGroupWithNativeTypeBound() throws Exception
	{
		final var basePackageDir = new File("src/test/generated/test/nt/nativegenerics");
		final var interfaceFile = new File(basePackageDir, "NativeContainer.java");

		assertTrue(interfaceFile.isFile(), "NativeContainer.java should be generated");

		final var interfaceContent = Files.readString(interfaceFile.toPath(), StandardCharsets.UTF_8);

		assertTrue(interfaceContent.contains("interface NativeContainer<T extends Float>"),
				   "Generic bound should map to native float (boxed Float)");
		assertTrue(interfaceContent.contains("List<T> value()"),
				   "Accessor should use the generic parameter inside the wrapper");

		final var floatParamFile = new File(basePackageDir, "FloatParameter.java");
		assertTrue(floatParamFile.isFile(), "FloatParameter.java should be generated");

		final var floatParamContent = Files.readString(floatParamFile.toPath(), StandardCharsets.UTF_8);
		assertTrue(floatParamContent.contains("interface FloatParameter extends NativeParameter<Float>"),
				   "Definition should specialize NativeParameter with float");

		final var nativeParameterFile = new File(basePackageDir, "NativeParameter.java");
		assertTrue(nativeParameterFile.isFile(), "NativeParameter.java should be generated");

		final var nativeParameterContent = Files.readString(nativeParameterFile.toPath(), StandardCharsets.UTF_8);
		assertTrue(nativeParameterContent.contains("default T getNativeValue()"),
				   "Operations should be generated on the declaring group");
		assertTrue(nativeParameterContent.contains("interface RFeatures<T extends RFeatures<T>> extends LMObject.RFeatures<T>"),
				   "Non-concrete group RFeatures interface should declare the self type parameter");

		assertFalse(floatParamContent.contains("getNativeValue("),
					"Operations declared on a parent group should not be redeclared on children");
		assertFalse(floatParamContent.contains("setNativeValue("),
					"Operations declared on a parent group should not be redeclared on children");

		assertTrue(floatParamContent.contains("interface RFeatures<T extends RFeatures<T>> extends NativeParameter.RFeatures<T>"),
				   "Concrete group RFeatures interface should keep the self type parameter when extending parents");

		final var floatParamImplFile = new File(basePackageDir, "impl/FloatParameterImpl.java");
		assertTrue(floatParamImplFile.isFile(), "FloatParameterImpl.java should be generated");
		final var floatParamImplContent = Files.readString(floatParamImplFile.toPath(), StandardCharsets.UTF_8);
		assertFalse(floatParamImplContent.contains("getNativeValue("),
					"Operation implementations should only be emitted on the declaring group");
	}
}

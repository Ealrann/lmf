package org.logoce.extender.impl.reflect.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public record MethodHandleContext(MethodHandle methodHandle, Class<?> declaringClass, MethodHandles.Lookup privateLookup) {}

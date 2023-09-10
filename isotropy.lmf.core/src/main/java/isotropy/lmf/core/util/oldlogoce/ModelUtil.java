package isotropy.lmf.core.util.oldlogoce;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.sheepy.lily.core.api.model.ILilyEObject;
import org.sheepy.lily.core.api.resource.IModelExtension;
import org.sheepy.lily.core.model.application.Application;
import org.sheepy.lily.core.model.application.ApplicationPackage;

import java.util.*;
import java.util.stream.Stream;

public final class ModelUtil
{
	private static final EClass APPLICATION_ECLASS = ApplicationPackage.Literals.APPLICATION;

	private ModelUtil()
	{
	}

	public static Application getApplication(EObject eObject)
	{
		while (eObject != null && APPLICATION_ECLASS.isInstance(eObject) == false)
		{
			eObject = eObject.eContainer();
		}

		return (Application) eObject;
	}

	public static EClass resolveEClass(Class<? extends EObject> classifier)
	{
		final String name = classifier.getSimpleName();
		final String pkgName = classifier.getPackageName();
		for (final IModelExtension extension : IModelExtension.EXTENSIONS)
		{
			for (final EPackage ePackage : extension.getEPackages())
			{
				final var epkg = ePackage.getClass().getPackageName().replaceAll(".impl", "");
				if (pkgName.equals(epkg))
				{
					final var res = (EClass) ePackage.getEClassifier(name);
					if (res != null)
					{
						return res;
					}
				}
			}
		}

		final var fromEcore = (EClass) EcorePackage.eINSTANCE.getEClassifier(name);
		if (fromEcore != null)
		{
			return fromEcore;
		}

		eClassNotFoundError(name);
		return null;
	}

	private static void eClassNotFoundError(String name) throws AssertionError
	{
		final String message = "Cannot find any EClass matching with %s";
		final String errorMessage = String.format(message, name);
		throw new AssertionError(errorMessage);
	}

	@SuppressWarnings("unchecked")
	public static <T extends EObject> List<T> gatherAllChildren(EObject root, Class<T> type)
	{
		final List<T> res = new ArrayList<>();
		final Deque<EObject> course = new ArrayDeque<>();

		course.add(root);

		while (course.isEmpty() == false)
		{
			final var currentEObject = course.pop();

			if (type.isInstance(currentEObject))
			{
				res.add((T) currentEObject);
			}

			gatherChildren(currentEObject, course);
		}

		return res;
	}

	public static <T> T findParent(EObject eo, Class<T> parentClassifier)
	{
		while (parentClassifier.isInstance(eo) == false && eo != null)
		{
			eo = eo.eContainer();
		}

		return parentClassifier.cast(eo);
	}

	public record ParentDescription<T extends EObject>(int distance, T parent)
	{
	}

	@SuppressWarnings("unchecked")
	public static <T extends EObject> ParentDescription<? extends T> parent(EObject eo, Class<T> parentClassifier)
	{
		int height = 0;
		while (parentClassifier.isInstance(eo) == false && eo != null)
		{
			height++;
			eo = eo.eContainer();
		}

		return new ParentDescription<>(height, (T) eo);
	}

	public static int parentDistance(EObject eo, Class<? extends EObject> parentClassifier)
	{
		int height = 0;
		while (parentClassifier.isInstance(eo) == false && eo != null)
		{
			height++;
			eo = eo.eContainer();
		}

		if (parentClassifier.isInstance(eo))
		{
			return height;
		}
		else
		{
			return -1;
		}
	}

	public static EClassifier resolveGenericType(EObject object, EClass genericHolder)
	{
		return resolveGenericType(object.eClass(), genericHolder);
	}

	private static EClassifier resolveGenericType(EClass eClass, EClass genericHolder)
	{
		EClassifier res = null;
		final var eGenericSuperTypes = eClass.getEGenericSuperTypes();

		for (int i = 0; i < eGenericSuperTypes.size(); i++)
		{
			final var gType = eGenericSuperTypes.get(i);
			final var classifier = gType.getEClassifier();
			if (genericHolder == classifier)
			{
				res = gType.getETypeArguments().get(0).getERawType();
				break;
			}
		}

		if (res == null)
		{
			for (final EClass superType : eClass.getESuperTypes())
			{
				res = resolveGenericType(superType, genericHolder);
				if (res != null)
				{
					break;
				}
			}
		}

		return res;
	}

	public static void gatherChildren(EObject eo, Collection<EObject> gatherIn)
	{
		gatherChildren(eo, EObject.class, gatherIn);
	}

	@SuppressWarnings("unchecked")
	public static <T extends EObject> void gatherChildren(EObject eo, Class<T> type, Collection<T> gatherIn)
	{
		final var containments = eo.eClass().getEAllContainments();
		for (int i = 0; i < containments.size(); i++)
		{
			final var eReference = containments.get(i);

			if (type == null || type == EObject.class || eReference.getEReferenceType()
																   .getInstanceClass()
																   .isAssignableFrom(type))
			{
				if (eReference.isMany())
				{
					final var values = (List<EObject>) eo.eGet(eReference);
					for (int j = 0; j < values.size(); j++)
					{
						final EObject value = values.get(j);
						if (value != null)
						{
							gatherIn.add((T) value);
						}
					}
				}
				else
				{
					final var value = (EObject) eo.eGet(eReference);
					if (value != null)
					{
						gatherIn.add((T) value);
					}
				}
			}
		}
	}

	public static Stream<ILilyEObject> streamChildren(ILilyEObject eo)
	{
		return eo.eClass()
				 .getEAllContainments()
				 .stream()
				 .flatMap(ref -> streamReference(eo, ref))
				 .filter(Objects::nonNull);
	}

	public static Stream<ILilyEObject> streamReferences(final ILilyEObject target, final int[] features)
	{
		Stream<ILilyEObject> stream = Stream.of(target);
		for (var feature : features)
		{
			stream = stream.flatMap(obj -> ModelUtil.streamReference(obj, feature));
		}
		return stream;
	}

	public static Stream<ILilyEObject> streamReference(ILilyEObject eo, int intReference)
	{
		final var ref = (EReference) eo.eClass().getEStructuralFeature(intReference);
		return streamReference(eo, ref);
	}

	@SuppressWarnings("unchecked")
	public static Stream<ILilyEObject> streamReference(ILilyEObject eo, EReference ref)
	{
		if (ref.isMany())
		{
			return ((List<ILilyEObject>) eo.eGet(ref)).stream();
		}
		else
		{
			return Stream.ofNullable((ILilyEObject) eo.eGet(ref));
		}
	}
	public static Optional<ContainmentPath> containmentPath(final EObject parent, EObject child)
	{
		final Deque<ChildReference> res = new ArrayDeque<>();
		while (child != null && child != parent && child.eContainer() != null)
		{
			res.addFirst(ChildReference.referenceFromParent(child));
			child = child.eContainer();
		}
		if (child == parent) return Optional.of(new ContainmentPath(res));
		else return Optional.empty();
	}

	public record ChildReference(EReference reference, int index)
	{
		public static ChildReference referenceFromParent(EObject child)
		{
			final var containmentFeature = child.eContainmentFeature();
			final var container = child.eContainer();
			final var many = containmentFeature.isMany();
			final int index = many ? ((EList<?>) container.eGet(containmentFeature)).indexOf(child) : 0;
			return new ChildReference(containmentFeature, index);
		}

		@SuppressWarnings("unchecked")
		public EObject eGet(EObject source)
		{
			if (source == null)
			{
				return null;
			}
			else if (reference.isMany())
			{
				return ((EList<EObject>) source.eGet(reference)).get(index);
			}
			else
			{
				return (EObject) source.eGet(reference);
			}
		}
	}

	public record ContainmentPath(Collection<ChildReference> path)
	{
		public EObject eGet(EObject source)
		{
			for (final var childReference : path)
			{
				source = childReference.eGet(source);
			}
			return source;
		}
	}
}

package isotropy.lmf.core.resource.transform.node;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;

import java.util.List;

public record BuilderNodeInfo<T extends LMObject>(Relation<T, ?> containingRelation,
												  List<String> words,
												  ModelGroup<T> modelGroup) {}

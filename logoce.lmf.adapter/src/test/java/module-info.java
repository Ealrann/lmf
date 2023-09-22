import org.logoce.lmf.adapter.test.Adapters;
import org.logoce.lmf.adapter.test.TestAdapter;
import org.logoce.lmf.extender.api.IAdapterProvider;
import org.logoce.lmf.extender.ext.IAdaptableNameMatcher;

open module logoce.adapter.test {
	requires logoce.lmf.adapter;

	requires org.junit.jupiter.api;

	provides IAdapterProvider with Adapters;
	provides IAdaptableNameMatcher with TestAdapter.TestNameMatcher;
}

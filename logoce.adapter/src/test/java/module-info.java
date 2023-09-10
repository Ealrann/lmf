import org.logoce.adapter.test.Adapters;
import org.logoce.adapter.test.TestAdapter;
import org.logoce.extender.api.IAdapterProvider;
import org.logoce.extender.ext.IAdaptableNameMatcher;

open module logoce.adapter.test {
	requires logoce.adapter;

	requires org.junit.jupiter.api;

	provides IAdapterProvider with Adapters;
	provides IAdaptableNameMatcher with TestAdapter.TestNameMatcher;
}

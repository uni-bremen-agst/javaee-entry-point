package soot.jimple.toolkits.javaee.model.servlet;

import java.text.Collator;
import java.util.Comparator;

public class NamedElementComparator<T extends NamedElement> implements Comparator<T> {
	private final Collator collator = Collator.getInstance();
	
	@Override
	public int compare(final T left, final T right) {
		return collator.compare(left.getName(), right.getName());
	}
}

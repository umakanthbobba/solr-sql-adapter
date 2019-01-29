
package org.apache.calcite.adapter.solr;

import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.fun.SqlCastFunction;

public class SqlFilter2SolrFilterTranslator {
	private final String[] solrFieldNames;

	public SqlFilter2SolrFilterTranslator(String[] solrFieldNames) {
		this.solrFieldNames = solrFieldNames;
	}

	private String translateColumn(RexInputRef ref) {
		return this.solrFieldNames[ref.getIndex()];
	}

	public SolrFilter translate(RexNode node) throws Exception {
		return processUnrecognied(processNOT(translateSqlFilter2SolrFilter(node)));
	}

	private RexNode trimColumnCast(RexNode node) {
		RexNode localRexNode1 = node;
		Object localObject1 = null;
		label:

		if ((localRexNode1 instanceof RexCall)) {
			RexCall localRexCall = (RexCall) localRexNode1;
			Tuple2 localTuple2 = new Tuple2(localRexCall.op, localRexCall.operands.get(0));
			if (localTuple2 != null) {
				RexNode ref = (RexNode) localTuple2.ref;
				if (((localTuple2.obj instanceof SqlCastFunction)) && ((ref instanceof RexInputRef))) {
					RexInputRef localRexInputRef = (RexInputRef) ref;
					localObject1 = localRexInputRef;
					break label;
				}
			}
			Object localObject2 = localRexCall;
			localObject1 = localObject2;
		} else {
			localObject1 = node;
		}

		return (RexNode) localObject1;

	}

	public SolrFilter processNOT(SolrFilter filter) throws Exception {
		SolrFilter localSolrFilter1 = filter;
		Object localObject1;
		if ((localSolrFilter1 instanceof AndSolrFilter)) {

			AndSolrFilter localAndSolrFilter1 = (AndSolrFilter) localSolrFilter1;
			SolrFilter left = localAndSolrFilter1.left;
			SolrFilter right = localAndSolrFilter1.right;
			localObject1 = new AndSolrFilter(processNOT(left), processNOT(right));
		} else if ((localSolrFilter1 instanceof OrSolrFilter)) {
			OrSolrFilter localOrSolrFilter1 = (OrSolrFilter) localSolrFilter1;
			SolrFilter left = localOrSolrFilter1.left;
			SolrFilter right = localOrSolrFilter1.right;
			localObject1 = new OrSolrFilter(processNOT(left), processNOT(right));
		} else if ((localSolrFilter1 instanceof NotSolrFilter)) {
			NotSolrFilter localNotSolrFilter1 = (NotSolrFilter) localSolrFilter1;
			SolrFilter left = localNotSolrFilter1.left;
			SolrFilter localSolrFilter2 = left;
			Object localObject2;
			if ((localSolrFilter2 instanceof AndSolrFilter)) {
				AndSolrFilter localAndSolrFilter2 = (AndSolrFilter) localSolrFilter2;
				left = localAndSolrFilter2.left;
				SolrFilter right = localAndSolrFilter2.right;
				localObject2 = new OrSolrFilter(processNOT(new NotSolrFilter(left)), processNOT(new NotSolrFilter(right)));
			} else if ((localSolrFilter2 instanceof OrSolrFilter)) {
				OrSolrFilter localOrSolrFilter2 = (OrSolrFilter) localSolrFilter2;
				left = localOrSolrFilter2.left;
				SolrFilter right = localOrSolrFilter2.right;
				localObject2 = new AndSolrFilter(processNOT(new NotSolrFilter(left)), processNOT(new NotSolrFilter(right)));
			} else if ((localSolrFilter2 instanceof NotSolrFilter)) {
				NotSolrFilter localNotSolrFilter2 = (NotSolrFilter) localSolrFilter2;
				left = localNotSolrFilter2.left;
				localObject2 = processNOT(left);
			} else if ((localSolrFilter2 instanceof GtSolrFilter)) {
				GtSolrFilter localGtSolrFilter = (GtSolrFilter) localSolrFilter2;
				String column = localGtSolrFilter.attributeName;
				Object value = localGtSolrFilter.value;
				localObject2 = new LeSolrFilter(column, value);
			} else if ((localSolrFilter2 instanceof GeSolrFilter)) {
				GeSolrFilter localGeSolrFilter = (GeSolrFilter) localSolrFilter2;
				String column = localGeSolrFilter.attributeName;
				Object value = localGeSolrFilter.value;
				localObject2 = new LtSolrFilter(column, value);
			} else if ((localSolrFilter2 instanceof LtSolrFilter)) {
				LtSolrFilter localLtSolrFilter = (LtSolrFilter) localSolrFilter2;
				String column = localLtSolrFilter.attributeName;
				Object value = localLtSolrFilter.value;
				localObject2 = new GeSolrFilter(column, value);
			} else if ((localSolrFilter2 instanceof LeSolrFilter)) {
				LeSolrFilter localLeSolrFilter = (LeSolrFilter) localSolrFilter2;
				String column = localLeSolrFilter.attributeName;
				Object value = localLeSolrFilter.value;
				localObject2 = new GtSolrFilter(column, value);
			} else if ((localSolrFilter2 instanceof EqualsSolrFilter)) {
				EqualsSolrFilter localEqualsSolrFilter = (EqualsSolrFilter) localSolrFilter2;
				String column = localEqualsSolrFilter.attributeName;
				Object value = localEqualsSolrFilter.value;
				localObject2 = new NotEqualsSolrFilter(column, value);
			} else if ((localSolrFilter2 instanceof NotEqualsSolrFilter)) {
				NotEqualsSolrFilter localNotEqualsSolrFilter = (NotEqualsSolrFilter) localSolrFilter2;
				String column = localNotEqualsSolrFilter.attributeName;
				Object value = localNotEqualsSolrFilter.value;
				localObject2 = new EqualsSolrFilter(column, value);
			} else if ((localSolrFilter2 instanceof NotNullSolrFilter)) {
				NotNullSolrFilter localNotNullSolrFilter = (NotNullSolrFilter) localSolrFilter2;
				String column = localNotNullSolrFilter.attributeName;
				localObject2 = new IsNullSolrFilter(column);
			} else {
				if (!(localSolrFilter2 instanceof IsNullSolrFilter)) {
					throw new Exception();
				}
				IsNullSolrFilter localIsNullSolrFilter = (IsNullSolrFilter) localSolrFilter2;
				String column = localIsNullSolrFilter.attributeName;
				localObject2 = new NotNullSolrFilter(column);
			}
			localObject1 = localObject2;
			return (SolrFilter) localObject1;

		}
		return filter;

	}

	public SolrFilter processUnrecognied(SolrFilter filter) {
		int i = 0;
		AndSolrFilter localAndSolrFilter = null;

		int j = 0;
		OrSolrFilter localOrSolrFilter = null;
		SolrFilter localSolrFilter1 = filter;
		SolrFilter localObject;
		if ((localSolrFilter1 instanceof AndSolrFilter)) {
			i = 1;
			localAndSolrFilter = (AndSolrFilter) localSolrFilter1;
			if (((localAndSolrFilter.left instanceof UnrecognizedSolrFilter)) && ((localAndSolrFilter.right instanceof UnrecognizedSolrFilter))) {
				localObject = new UnrecognizedSolrFilter();
				return (SolrFilter) localObject;
			}
		}
		if (i != 0) {
			SolrFilter left = localAndSolrFilter.left;
			if ((localAndSolrFilter.right instanceof UnrecognizedSolrFilter)) {
				localObject = left;
				return (SolrFilter) localObject;
			}
		}
		if (i != 0) {
			SolrFilter right = localAndSolrFilter.right;
			if ((localAndSolrFilter.left instanceof UnrecognizedSolrFilter)) {
				localObject = right;
				return (SolrFilter) localObject;
			}
		}
		if ((localSolrFilter1 instanceof OrSolrFilter)) {
			j = 1;
			localOrSolrFilter = (OrSolrFilter) localSolrFilter1;
			if ((localOrSolrFilter.left instanceof UnrecognizedSolrFilter)) {
				localObject = new UnrecognizedSolrFilter();
				return (SolrFilter) localObject;
			}
		}
		if (j != 0) {
			if ((localOrSolrFilter.right instanceof UnrecognizedSolrFilter)) {
				localObject = new UnrecognizedSolrFilter();
				return (SolrFilter) localObject;
			}
		}
		return filter;

	}

	private SolrFilter translateSqlFilter2SolrFilter(RexNode node) throws Exception {
		RexNode localRexNode1 = node;

		if ((localRexNode1 instanceof RexCall)) {
			RexNode left = trimColumnCast((RexNode) ((RexCall) node).operands.get(0));
			RexNode right = ((RexCall) node).operands.size() > 1 ? trimColumnCast((RexNode) ((RexCall) node).operands.get(1)) : null;
			Tuple3 localTuple3 = new Tuple3(node.getKind(), left, right);
			SolrFilter localObject2;
			if (localTuple3 != null) {
				SqlKind localSqlKind1 = (SqlKind) localTuple3.kind;
				if (SqlKind.AND.equals(localSqlKind1)) {
					localObject2 = new AndSolrFilter(translate(left), translate(right));
					return (SolrFilter) localObject2;
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind2 = (SqlKind) localTuple3.kind;
				if (SqlKind.OR.equals(localSqlKind2)) {
					localObject2 = new OrSolrFilter(translate(left), translate(right));
					return (SolrFilter) localObject2;
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind3 = (SqlKind) localTuple3.kind;
				RexNode ref = (RexNode) localTuple3.left;
				RexNode localRexNode2 = (RexNode) localTuple3.right;
				if ((SqlKind.IS_NULL.equals(localSqlKind3)) && ((ref instanceof RexInputRef))) {
					RexInputRef localRexInputRef1 = (RexInputRef) ref;
					if (localRexNode2 == null) {
						localObject2 = new IsNullSolrFilter(translateColumn(localRexInputRef1));
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind4 = (SqlKind) localTuple3.kind;
				RexNode ref = (RexNode) localTuple3.left;
				RexNode localRexNode3 = (RexNode) localTuple3.right;
				if ((SqlKind.IS_NOT_NULL.equals(localSqlKind4)) && ((ref instanceof RexInputRef))) {
					RexInputRef localRexInputRef2 = (RexInputRef) ref;
					if (localRexNode3 == null) {
						localObject2 = new NotNullSolrFilter(translateColumn(localRexInputRef2));
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind5 = (SqlKind) localTuple3.kind;
				RexNode ref = (RexNode) localTuple3.left;
				RexNode lit = (RexNode) localTuple3.right;
				if ((SqlKind.GREATER_THAN.equals(localSqlKind5)) && ((ref instanceof RexInputRef))) {
					RexInputRef localRexInputRef3 = (RexInputRef) ref;
					if ((lit instanceof RexLiteral)) {
						RexLiteral localRexLiteral1 = (RexLiteral) lit;
						localObject2 = new GtSolrFilter(translateColumn(localRexInputRef3), localRexLiteral1.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind6 = (SqlKind) localTuple3.kind;
				RexNode lit = (RexNode) localTuple3.left;
				RexNode ref = (RexNode) localTuple3.right;
				if ((SqlKind.GREATER_THAN.equals(localSqlKind6)) && ((lit instanceof RexLiteral))) {
					RexLiteral localRexLiteral2 = (RexLiteral) lit;
					if ((ref instanceof RexInputRef)) {
						RexInputRef localRexInputRef4 = (RexInputRef) ref;
						localObject2 = new LeSolrFilter(translateColumn(localRexInputRef4), localRexLiteral2.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind7 = (SqlKind) localTuple3.kind;
				RexNode ref = (RexNode) localTuple3.left;
				RexNode lit = (RexNode) localTuple3.right;
				if ((SqlKind.LESS_THAN.equals(localSqlKind7)) && ((ref instanceof RexInputRef))) {
					RexInputRef localRexInputRef5 = (RexInputRef) ref;
					if ((lit instanceof RexLiteral)) {
						RexLiteral localRexLiteral3 = (RexLiteral) lit;
						localObject2 = new LtSolrFilter(translateColumn(localRexInputRef5), localRexLiteral3.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind8 = (SqlKind) localTuple3.kind;
				RexNode lit = (RexNode) localTuple3.left;
				RexNode ref = (RexNode) localTuple3.right;
				if ((SqlKind.LESS_THAN.equals(localSqlKind8)) && ((lit instanceof RexLiteral))) {
					RexLiteral localRexLiteral4 = (RexLiteral) lit;
					if ((ref instanceof RexInputRef)) {
						RexInputRef localRexInputRef6 = (RexInputRef) ref;
						localObject2 = new GeSolrFilter(translateColumn(localRexInputRef6), localRexLiteral4.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind9 = (SqlKind) localTuple3.kind;
				RexNode ref = (RexNode) localTuple3.left;
				RexNode lit = (RexNode) localTuple3.right;
				if ((SqlKind.GREATER_THAN_OR_EQUAL.equals(localSqlKind9)) && ((ref instanceof RexInputRef))) {
					RexInputRef localRexInputRef7 = (RexInputRef) ref;
					if ((lit instanceof RexLiteral)) {
						RexLiteral localRexLiteral5 = (RexLiteral) lit;
						localObject2 = new GeSolrFilter(translateColumn(localRexInputRef7), localRexLiteral5.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind10 = (SqlKind) localTuple3.kind;
				RexNode lit = (RexNode) localTuple3.left;
				RexNode ref = (RexNode) localTuple3.right;
				if ((SqlKind.GREATER_THAN_OR_EQUAL.equals(localSqlKind10)) && ((lit instanceof RexLiteral))) {
					RexLiteral localRexLiteral6 = (RexLiteral) lit;
					if ((ref instanceof RexInputRef)) {
						RexInputRef localRexInputRef8 = (RexInputRef) ref;
						localObject2 = new LtSolrFilter(translateColumn(localRexInputRef8), localRexLiteral6.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind11 = (SqlKind) localTuple3.kind;
				RexNode ref = (RexNode) localTuple3.left;
				RexNode lit = (RexNode) localTuple3.right;
				if ((SqlKind.LESS_THAN_OR_EQUAL.equals(localSqlKind11)) && ((ref instanceof RexInputRef))) {
					RexInputRef localRexInputRef9 = (RexInputRef) ref;
					if ((lit instanceof RexLiteral)) {
						RexLiteral localRexLiteral7 = (RexLiteral) lit;
						localObject2 = new LeSolrFilter(translateColumn(localRexInputRef9), localRexLiteral7.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind12 = (SqlKind) localTuple3.kind;
				RexNode lit = (RexNode) localTuple3.left;
				RexNode ref = (RexNode) localTuple3.right;
				if ((SqlKind.LESS_THAN_OR_EQUAL.equals(localSqlKind12)) && ((lit instanceof RexLiteral))) {
					RexLiteral localRexLiteral8 = (RexLiteral) lit;
					if ((ref instanceof RexInputRef)) {
						RexInputRef localRexInputRef10 = (RexInputRef) ref;
						localObject2 = new GtSolrFilter(translateColumn(localRexInputRef10), localRexLiteral8.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind13 = (SqlKind) localTuple3.kind;
				RexNode lit = (RexNode) localTuple3.left;
				RexNode ref = (RexNode) localTuple3.right;
				if ((SqlKind.EQUALS.equals(localSqlKind13)) && ((lit instanceof RexLiteral))) {
					RexLiteral localRexLiteral9 = (RexLiteral) lit;
					if ((ref instanceof RexInputRef)) {
						RexInputRef localRexInputRef11 = (RexInputRef) ref;
						localObject2 = new EqualsSolrFilter(translateColumn(localRexInputRef11), localRexLiteral9.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind14 = (SqlKind) localTuple3.kind;
				RexNode ref = (RexNode) localTuple3.left;
				RexNode lit = (RexNode) localTuple3.right;
				if ((SqlKind.EQUALS.equals(localSqlKind14)) && ((ref instanceof RexInputRef))) {
					RexInputRef localRexInputRef12 = (RexInputRef) ref;
					if ((lit instanceof RexLiteral)) {
						RexLiteral localRexLiteral10 = (RexLiteral) lit;
						localObject2 = new EqualsSolrFilter(translateColumn(localRexInputRef12), localRexLiteral10.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind15 = (SqlKind) localTuple3.kind;
				RexNode lit = (RexNode) localTuple3.left;
				RexNode ref = (RexNode) localTuple3.right;
				if ((SqlKind.LIKE.equals(localSqlKind15)) && ((lit instanceof RexLiteral))) {
					RexLiteral localRexLiteral11 = (RexLiteral) lit;
					if ((ref instanceof RexInputRef)) {
						RexInputRef localRexInputRef13 = (RexInputRef) ref;
						localObject2 = new LikeSolrFilter(translateColumn(localRexInputRef13), localRexLiteral11.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind16 = (SqlKind) localTuple3.kind;
				RexNode ref = (RexNode) localTuple3.left;
				RexNode lit = (RexNode) localTuple3.right;
				if ((SqlKind.LIKE.equals(localSqlKind16)) && ((ref instanceof RexInputRef))) {
					RexInputRef localRexInputRef14 = (RexInputRef) ref;
					if ((lit instanceof RexLiteral)) {
						RexLiteral localRexLiteral12 = (RexLiteral) lit;
						localObject2 = new LikeSolrFilter(translateColumn(localRexInputRef14), localRexLiteral12.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind17 = (SqlKind) localTuple3.kind;
				RexNode lit = (RexNode) localTuple3.left;
				RexNode ref = (RexNode) localTuple3.right;
				if ((SqlKind.NOT_EQUALS.equals(localSqlKind17)) && ((lit instanceof RexLiteral))) {
					RexLiteral localRexLiteral13 = (RexLiteral) lit;
					if ((ref instanceof RexInputRef)) {
						RexInputRef localRexInputRef15 = (RexInputRef) ref;
						localObject2 = new NotEqualsSolrFilter(translateColumn(localRexInputRef15), localRexLiteral13.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind18 = (SqlKind) localTuple3.kind;
				RexNode ref = (RexNode) localTuple3.left;
				RexNode lit = (RexNode) localTuple3.right;
				if ((SqlKind.NOT_EQUALS.equals(localSqlKind18)) && ((ref instanceof RexInputRef))) {
					RexInputRef localRexInputRef16 = (RexInputRef) ref;
					if ((lit instanceof RexLiteral)) {
						RexLiteral localRexLiteral14 = (RexLiteral) lit;
						localObject2 = new NotEqualsSolrFilter(translateColumn(localRexInputRef16), localRexLiteral14.getValue2());
						return (SolrFilter) localObject2;
					}
				}
			}
			if (localTuple3 != null) {
				SqlKind localSqlKind19 = (SqlKind) localTuple3.kind;
				RexNode localRexNode4 = (RexNode) localTuple3.right;
				if ((SqlKind.NOT.equals(localSqlKind19)) && (localRexNode4 == null)) {
					localObject2 = new NotSolrFilter(translate(left));
					return (SolrFilter) localObject2;
				}
			}

		}
		return null;
	}

	public static class Tuple2 {
		RexNode ref;
		Object obj;

		public Tuple2(Object obj, RexNode ref) {
			this.ref = ref;
			this.obj = obj;
		}
	}

	public static class UnrecognizedSolrFilter extends SolrFilter {
		@Override
		public String toSolrQueryString() {
			return "*:*";
		}
	}

	public static class AndSolrFilter extends SolrFilter {
		SolrFilter left, right;

		public AndSolrFilter(SolrFilter left, SolrFilter right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public String toSolrQueryString() {
			return left.toSolrQueryString() + " AND " + right.toSolrQueryString();
		}
	}

	public static class NotSolrFilter extends SolrFilter {

		SolrFilter left;

		public NotSolrFilter(SolrFilter left) {
			this.left = left;

		}

		@Override
		public String toSolrQueryString() {
			// throw new SolrSqlException("should never be called: " + left);
			return " SolrSqlException";
		}
	}

	public static class OrSolrFilter extends SolrFilter {
		SolrFilter left, right;

		public OrSolrFilter(SolrFilter left, SolrFilter right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public String toSolrQueryString() {
			return left.toSolrQueryString() + " OR " + right.toSolrQueryString();
		}
	}

	public static class GtSolrFilter extends SolrFilter {
		String attributeName;
		Object value;

		public GtSolrFilter(String attributeName, Object value) {
			this.attributeName = attributeName;
			this.value = value;
		}

		@Override
		public String toSolrQueryString() {
			return attributeName + " :{" + value + " TO *}";
		}
	}

	public static class NotNullSolrFilter extends SolrFilter {
		String attributeName;

		public NotNullSolrFilter(String attributeName) {
			this.attributeName = attributeName;

		}

		@Override
		public String toSolrQueryString() {
			return attributeName + ":*";
		}
	}

	public static class IsNullSolrFilter extends SolrFilter {
		String attributeName;

		public IsNullSolrFilter(String attributeName) {
			this.attributeName = attributeName;

		}

		@Override
		public String toSolrQueryString() {
			return "NOT " + attributeName + ":*";
		}
	}

	public static class EqualsSolrFilter extends SolrFilter {
		String attributeName;
		Object value;

		public EqualsSolrFilter(String attributeName, Object value) {
			this.attributeName = attributeName;
			this.value = value;
		}

		@Override
		public String toSolrQueryString() {
			return attributeName + ":" + value;
		}
	}

	public static class LikeSolrFilter extends SolrFilter {
		String attributeName;
		Object value;

		public LikeSolrFilter(String attributeName, Object value) {
			this.attributeName = attributeName;
			this.value = value;
		}

		String value2 = value.toString().replaceAll("%", "*").replaceAll("_", "?");

		@Override
		public String toSolrQueryString() {
			return attributeName + ":" + value2;
		}
	}

	public static class NotEqualsSolrFilter extends SolrFilter {
		String attributeName;
		Object value;

		public NotEqualsSolrFilter(String attributeName, Object value) {
			this.attributeName = attributeName;
			this.value = value;
		}

		@Override
		public String toSolrQueryString() {
			return "NOT " + attributeName + ":" + value;
		}
	}

	public static class GeSolrFilter extends SolrFilter {
		String attributeName;
		Object value;

		public GeSolrFilter(String attributeName, Object value) {
			this.attributeName = attributeName;
			this.value = value;
		}

		@Override
		public String toSolrQueryString() {
			return attributeName + ":[" + value + " TO *]";
		}
	}

	public static class LeSolrFilter extends SolrFilter {
		String attributeName;
		Object value;

		public LeSolrFilter(String attributeName, Object value) {
			this.attributeName = attributeName;
			this.value = value;
		}

		@Override
		public String toSolrQueryString() {
			return attributeName + ":[* TO " + value + "]";
		}
	}

	public static class LtSolrFilter extends SolrFilter {
		String attributeName;
		Object value;

		public LtSolrFilter(String attributeName, Object value) {
			this.attributeName = attributeName;
			this.value = value;
		}

		@Override
		public String toSolrQueryString() {
			return attributeName + ":{* TO " + value + "}";
		}

	}

	public static class Tuple3 {

		SqlKind kind;
		RexNode left, right;

		public Tuple3(SqlKind kind, RexNode left, RexNode right) {
			this.kind = kind;
			this.left = left;
			this.right = right;
		}

	}

}

package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo_;
import org.fao.geonet.domain.Metadata_;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

/**
 * An interface that creates the selection object that creates the calculation for {@link MetadataStatisticsQueries} methods.
 * <p/>
 * Some statndard implementations are in {@link MetadataStatisticSpec.StandardSpecs}
 * <p/>
 * User: Jesse
 * Date: 9/27/13
 * Time: 1:22 PM
 *
 * @see MetadataStatisticsQueries
 * @see MetadataStatisticSpec.StandardSpecs
 */
public interface MetadataStatisticSpec {

    /**
     * Get the selection object (the calculation).
     *
     * @param criteriaBuilder the criteria builder
     * @param root            the query metadata root
     * @return the selection object (the calculation).
     */
    Expression<? extends Number> getSelection(CriteriaBuilder criteriaBuilder, Root<Metadata> root);

    public final class StandardSpecs {
        private StandardSpecs() {
            // this should not be instantiated.  Just static methods
        }

        public static MetadataStatisticSpec metadataCount() {
            return new MetadataStatisticSpec() {
                @Override
                public Expression<Long> getSelection(CriteriaBuilder criteriaBuilder, Root<Metadata> root) {
                    return criteriaBuilder.count(root);
                }
            };
        }

        public static MetadataStatisticSpec popularitySum() {
            return new MetadataStatisticSpec() {
                @Override
                public Expression<Integer> getSelection(CriteriaBuilder criteriaBuilder, Root<Metadata> root) {
                    return criteriaBuilder.sum(root.get(Metadata_.dataInfo).get(MetadataDataInfo_.popularity));
                }
            };
        }

        public static MetadataStatisticSpec ratingSum() {
            return new MetadataStatisticSpec() {
                @Override
                public Expression<Integer> getSelection(CriteriaBuilder criteriaBuilder, Root<Metadata> root) {
                    return criteriaBuilder.sum(root.get(Metadata_.dataInfo).get(MetadataDataInfo_.rating));
                }
            };
        }
    }
}

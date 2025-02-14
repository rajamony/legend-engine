// Copyright 2022 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.persistence.components.ingestmode;

import org.finos.legend.engine.persistence.components.ingestmode.audit.Auditing;
import org.finos.legend.engine.persistence.components.ingestmode.deduplication.DeduplicationStrategy;
import org.finos.legend.engine.persistence.components.ingestmode.deduplication.DeduplicationStrategyVisitors;

import java.util.List;
import java.util.Optional;

import static org.immutables.value.Value.Check;
import static org.immutables.value.Value.Immutable;
import static org.immutables.value.Value.Style;

@Immutable
@Style(
    typeAbstract = "*Abstract",
    typeImmutable = "*",
    jdkOnly = true,
    optionalAcceptNullable = true,
    strictBuilder = true
)
public interface AppendOnlyAbstract extends IngestMode
{
    Optional<String> digestField();

    List<String> keyFields();

    Optional<String> dataSplitField();

    Auditing auditing();

    DeduplicationStrategy deduplicationStrategy();

    @Check
    default void validate()
    {
        if (deduplicationStrategy().accept(DeduplicationStrategyVisitors.IS_ALLOW_DUPLICATES))
        {
            if (!keyFields().isEmpty())
            {
                throw new IllegalStateException("Cannot build AppendOnly, [keyFields] must be empty since [deduplicationStrategy] is set to allow duplicates");
            }
        }
        else if (deduplicationStrategy().accept(DeduplicationStrategyVisitors.IS_FILTER_DUPLICATES))
        {
            if (!digestField().isPresent())
            {
                throw new IllegalStateException("Cannot build AppendOnly, [digestField] must be specified since [deduplicationStrategy] is set to filter duplicates");
            }
            if (keyFields().isEmpty())
            {
                throw new IllegalStateException("Cannot build AppendOnly, [keyFields] must contain at least one element since [deduplicationStrategy] is set to filter duplicates");
            }
        }
        else if (deduplicationStrategy().accept(DeduplicationStrategyVisitors.IS_FAIL_ON_DUPLICATES))
        {
            if (keyFields().isEmpty())
            {
                throw new IllegalStateException("Cannot build AppendOnly, [keyFields] must contain at least one element since [deduplicationStrategy] is set to fail on duplicates");
            }
        }
        else
        {
            throw new IllegalStateException("Unrecognized [deduplicationStrategy]: " + deduplicationStrategy().getClass());
        }
    }

    @Override
    default <T> T accept(IngestModeVisitor<T> visitor)
    {
        return visitor.visitAppendOnly(this);
    }
}

/*
 * Copyright 2011-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zqykj.annotations;

import java.lang.annotation.*;

/**
 * Container annotation that allows to collect multiple {@link CompoundIndex} annotations.
 * <p>
 * Can be used natively, declaring several nested {@link CompoundIndex} annotations. Can also be used in conjunction
 * with Java 8's support for <em>repeatable annotations</em>, where {@link CompoundIndex} can simply be declared several
 * times on the same {@linkplain ElementType#TYPE type}, implicitly generating this container annotation.
 *
 * @author Jon Brisbin
 * @author Christoph Strobl
 */
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface CompoundIndexes {

	CompoundIndex[] value();

}

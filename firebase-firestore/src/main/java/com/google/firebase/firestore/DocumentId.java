// Copyright 2019 Google LLC
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

package com.google.firebase.firestore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a POJO property to be automatically populated with the document's ID when
 * the POJO is created from a Cloud Firestore document (for example, via {@link
 * DocumentSnapshot#toObject(Class)}).
 *
 * <p>Any of the following will throw a runtime exception:
 *
 * <ul>
 *   <li>This annotation is applied to a property of a type other than String or <br>
 *       {@link DocumentReference}.
 *   <li>This annotation is applied to a property that is not writable (for example, a Java Bean
 *       getter without a backing field).
 *   <li>This annotation is applied to a property with a name that conflicts with a read document
 *       field. For example, if a POJO has a field `firstName` annotated by {@code @DocumentId}, and
 *       there is a property from the document named `firstName` as well, an exception is thrown
 *       when you try to read the document into the POJO via {@link
 *       DocumentSnapshot#toObject(Class)} or {@link DocumentReference#get()}.
 *   <li>
 * </ul>
 *
 * <p>When using a POJO to write to a document (via {@link DocumentReference#set(Object)} or @{@link
 * WriteBatch#set(DocumentReference, Object)}), the property annotated by {@code @DocumentId} is
 * ignored, which allows writing the POJO back to any document, even if it's not the origin of the
 * POJO.
 *
 * <h3>Kotlin Note</h3>
 * When applying this annotation to a property of a Kotlin class, the {@code @set} use-site target
 * should always be used. There is no need to use the {@code @get} use-site target as this
 * annotation is <em>only</em> considered when <em>reading</em> instances from Firestore, and is
 * ignored when <em>writing</em> instances into Firestore.
 * <p>
 * Here is an example of a class that can both be written into and read from Firestore whose
 * {@code foo} property will be populated with the Document ID when being read and will be ignored
 * when being written into Firestore:
 * <pre>
 * data class Pojo(@set:DocumentId var foo: String? = null) {
 *   constructor() : this(null) // Used by Firestore to create new instances
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface DocumentId {}

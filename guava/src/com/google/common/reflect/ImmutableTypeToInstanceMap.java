/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.reflect;

import static java.util.Collections.unmodifiableMap;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * A type-to-instance map backed by an {@link ImmutableMap}. See also {@link
 * MutableTypeToInstanceMap}.
 *
 * @author Ben Yu
 * @since 13.0
 */
@Beta
public final class ImmutableTypeToInstanceMap<B>
    extends ForwardingMap<TypeToken<? extends B>, @Nullable B> implements TypeToInstanceMap<B> {

  /** Returns an empty type to instance map. */
  public static <B> ImmutableTypeToInstanceMap<B> of() {
    return new ImmutableTypeToInstanceMap<B>(ImmutableMap.<TypeToken<? extends B>, B>of());
  }

  /** Returns a new builder. */
  public static <B> Builder<B> builder() {
    return new Builder<B>();
  }

  /**
   * A builder for creating immutable type-to-instance maps. Example:
   *
   * <pre>{@code
   * static final ImmutableTypeToInstanceMap<Handler<?>> HANDLERS =
   *     ImmutableTypeToInstanceMap.<Handler<?>>builder()
   *         .put(new TypeToken<Handler<Foo>>() {}, new FooHandler())
   *         .put(new TypeToken<Handler<Bar>>() {}, new SubBarHandler())
   *         .build();
   * }</pre>
   *
   * <p>After invoking {@link #build()} it is still possible to add more entries and build again.
   * Thus each map generated by this builder will be a superset of any map generated before it.
   *
   * @since 13.0
   */
  @Beta
  public static final class Builder<B> {
    private final ImmutableMap.Builder<TypeToken<? extends B>, B> mapBuilder =
        ImmutableMap.builder();

    private Builder() {}

    /**
     * Associates {@code key} with {@code value} in the built map. Duplicate keys are not allowed,
     * and will cause {@link #build} to fail.
     */
    @CanIgnoreReturnValue
    public <T extends B> Builder<B> put(Class<T> key, T value) {
      mapBuilder.put(TypeToken.of(key), value);
      return this;
    }

    /**
     * Associates {@code key} with {@code value} in the built map. Duplicate keys are not allowed,
     * and will cause {@link #build} to fail.
     */
    @CanIgnoreReturnValue
    public <T extends B> Builder<B> put(TypeToken<T> key, T value) {
      mapBuilder.put(key.rejectTypeVariables(), value);
      return this;
    }

    /**
     * Returns a new immutable type-to-instance map containing the entries provided to this builder.
     *
     * @throws IllegalArgumentException if duplicate keys were added
     */
    public ImmutableTypeToInstanceMap<B> build() {
      return new ImmutableTypeToInstanceMap<B>(mapBuilder.build());
    }
  }

  private final Map<TypeToken<? extends B>, @Nullable B> delegate;

  private ImmutableTypeToInstanceMap(ImmutableMap<TypeToken<? extends B>, B> delegate) {
    // Convert from Map<..., B> to Map<..., @Nullable B>.
    this.delegate = unmodifiableMap(delegate);
  }

  @Override
  public <T extends B> @Nullable T getInstance(TypeToken<T> type) {
    return trustedGet(type.rejectTypeVariables());
  }

  @Override
  public <T extends B> @Nullable T getInstance(Class<T> type) {
    return trustedGet(TypeToken.of(type));
  }

  /**
   * Guaranteed to throw an exception and leave the map unmodified.
   *
   * @deprecated unsupported operation
   * @throws UnsupportedOperationException always
   */
  @CanIgnoreReturnValue
  @Deprecated
  @Override
  public <T extends B> @Nullable T putInstance(TypeToken<T> type, @Nullable T value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the map unmodified.
   *
   * @deprecated unsupported operation
   * @throws UnsupportedOperationException always
   */
  @CanIgnoreReturnValue
  @Deprecated
  @Override
  public <T extends B> @Nullable T putInstance(Class<T> type, @Nullable T value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the map unmodified.
   *
   * @deprecated unsupported operation
   * @throws UnsupportedOperationException always
   */
  @CanIgnoreReturnValue
  @Deprecated
  @Override
  public @Nullable B put(TypeToken<? extends B> key, @Nullable B value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the map unmodified.
   *
   * @deprecated unsupported operation
   * @throws UnsupportedOperationException always
   */
  @Deprecated
  @Override
  public void putAll(Map<? extends TypeToken<? extends B>, ? extends @Nullable B> map) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Map<TypeToken<? extends B>, @Nullable B> delegate() {
    return delegate;
  }

  @SuppressWarnings("unchecked") // value could not get in if not a T
  private <T extends B> @Nullable T trustedGet(TypeToken<T> type) {
    return (T) delegate.get(type);
  }
}

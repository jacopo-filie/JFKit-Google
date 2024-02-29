//
//	The MIT License (MIT)
//
//	Copyright © 2017-2024 Jacopo Filié
//
//	Permission is hereby granted, free of charge, to any person obtaining a copy
//	of this software and associated documentation files (the "Software"), to deal
//	in the Software without restriction, including without limitation the rights
//	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//	copies of the Software, and to permit persons to whom the Software is
//	furnished to do so, subject to the following conditions:
//
//	The above copyright notice and this permission notice shall be included in all
//	copies or substantial portions of the Software.
//
//	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//	SOFTWARE.
//

package com.jackfelle.jfkit;

import com.jackfelle.jfkit.functions.BooleanSupplier;
import com.jackfelle.jfkit.functions.Consumer;
import com.jackfelle.jfkit.functions.Function;
import com.jackfelle.jfkit.functions.OptionalOutputFunction;
import com.jackfelle.jfkit.functions.OptionalSupplier;
import com.jackfelle.jfkit.functions.Predicate;
import com.jackfelle.jfkit.functions.Supplier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class If
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods
	
	public static <T> void let(@Nullable T object, @NonNull Consumer<T> letBlock) {
		If.let(object, letBlock, null);
	}
	
	public static <T> void let(@Nullable T object, @NonNull Consumer<T> letBlock, @Nullable Runnable elseBlock) {
		if(object != null) {
			letBlock.consume(object);
		} else if(elseBlock != null) {
			elseBlock.run();
		}
	}
	
	public static <T> void letAs(@Nullable Object object, @NonNull Class<T> type, @NonNull Consumer<T> letBlock) {
		If.let(Filter.byType(object, type), letBlock, null);
	}
	
	public static <T> void letAs(@Nullable Object object, @NonNull Class<T> type, @NonNull Consumer<T> letBlock, @Nullable Runnable elseBlock) {
		If.let(Filter.byType(object, type), letBlock, elseBlock);
	}
	
	public static @NonNull <T, R> R letAsGet(@Nullable Object object, @NonNull Class<T> type, @NonNull Function<T, R> letBlock, @NonNull R replacement) {
		return If.letGet(Filter.byType(object, type), letBlock, replacement);
	}
	
	public static @NonNull <T, R> R letAsGet(@Nullable Object object, @NonNull Class<T> type, @NonNull Function<T, R> letBlock, @NonNull Supplier<R> elseBlock) {
		return If.letGet(Filter.byType(object, type), letBlock, elseBlock);
	}
	
	public static <T> boolean letAsGetBoolean(@Nullable Object object, @NonNull Class<T> type, @NonNull Predicate<T> letBlock, boolean replacement) {
		return If.letGetBoolean(Filter.byType(object, type), letBlock, replacement);
	}
	
	public static <T> boolean letAsGetBoolean(@Nullable Object object, @NonNull Class<T> type, @NonNull Predicate<T> letBlock, @NonNull BooleanSupplier elseBlock) {
		return If.letGetBoolean(Filter.byType(object, type), letBlock, elseBlock);
	}
	
	public static @Nullable <T, R> R letAsGetOptional(@Nullable Object object, @NonNull Class<T> type, @NonNull OptionalOutputFunction<T, R> letBlock) {
		return If.letGetOptional(Filter.byType(object, type), letBlock);
	}
	
	public static @Nullable <T, R> R letAsGetOptional(@Nullable Object object, @NonNull Class<T> type, @NonNull OptionalOutputFunction<T, R> letBlock, @Nullable OptionalSupplier<R> elseBlock) {
		return If.letGetOptional(Filter.byType(object, type), letBlock, elseBlock);
	}
	
	public static @NonNull <T, R> R letGet(@Nullable T object, @NonNull Function<T, R> letBlock, @NonNull R replacement) {
		return (object != null) ? letBlock.apply(object) : replacement;
	}
	
	public static @NonNull <T, R> R letGet(@Nullable T object, @NonNull Function<T, R> letBlock, @NonNull Supplier<R> elseBlock) {
		return (object != null) ? letBlock.apply(object) : elseBlock.supply();
	}
	
	public static <T> boolean letGetBoolean(@Nullable T object, @NonNull Predicate<T> letBlock, boolean replacement) {
		return (object != null) ? letBlock.test(object) : replacement;
	}
	
	public static <T> boolean letGetBoolean(@Nullable T object, @NonNull Predicate<T> letBlock, @NonNull BooleanSupplier elseBlock) {
		return (object != null) ? letBlock.test(object) : elseBlock.supply();
	}
	
	public static @Nullable <T, R> R letGetOptional(@Nullable T object, @NonNull OptionalOutputFunction<T, R> letBlock) {
		return (object != null) ? letBlock.apply(object) : null;
	}
	
	public static @Nullable <T, R> R letGetOptional(@Nullable T object, @NonNull OptionalOutputFunction<T, R> letBlock, @Nullable OptionalSupplier<R> elseBlock) {
		if(object != null) {
			return letBlock.apply(object);
		} else if(elseBlock != null) {
			return elseBlock.supply();
		} else {
			return null;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}

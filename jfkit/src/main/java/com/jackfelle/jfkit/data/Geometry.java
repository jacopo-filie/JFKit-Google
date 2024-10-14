//
//	The MIT License (MIT)
//
//	Copyright © 2018-2024 Jacopo Filié
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

package com.jackfelle.jfkit.data;

import com.jackfelle.jfkit.utilities.Utilities;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Geometry
{
	public static class EdgeInsets implements MutableCopying
	{
		protected float bottom;
		protected float left;
		protected float right;
		protected float top;
		
		public float getBottom() {
			return this.bottom;
		}
		
		public float getLeft() {
			return this.left;
		}
		
		public float getRight() {
			return this.right;
		}
		
		public float getTop() {
			return this.top;
		}
		
		public EdgeInsets(float left, float top, float right, float bottom) {
			this.bottom = bottom;
			this.left = left;
			this.right = right;
			this.top = top;
		}
		
		public EdgeInsets(float inset) {
			this(inset, inset, inset, inset);
		}
		
		protected EdgeInsets(@NonNull EdgeInsets source) {
			this(source.getLeft(), source.getTop(), source.getRight(), source.getBottom());
		}
		
		@Override public boolean equals(@Nullable Object obj) {
			if(obj == this) {
				return true;
			}
			
			if(!(obj instanceof EdgeInsets)) {
				return false;
			}
			
			EdgeInsets object = (EdgeInsets)obj;
			
			if(Float.compare(this.bottom, object.bottom) != 0) {
				return false;
			}
			
			if(Float.compare(this.left, object.left) != 0) {
				return false;
			}
			
			if(Float.compare(this.right, object.right) != 0) {
				return false;
			}
			
			return (Float.compare(this.top, object.top) == 0);
		}
		
		@Override public int hashCode() {
			return Utilities.hashCode(this.bottom, this.left, this.right, this.top);
		}
		
		@Override public @NonNull String toString() {
			return String.format(Locale.US, "%s{%g, %g, %g, %g}", this.getClass().getSimpleName(), this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
		}
		
		@Override public @NonNull EdgeInsets copy() {
			return this;
		}
		
		@Override public @NonNull MutableEdgeInsets mutableCopy() {
			return new MutableEdgeInsets(this);
		}
	}
	
	public static class MutableEdgeInsets extends EdgeInsets
	{
		public void setBottom(float bottom) {
			this.bottom = bottom;
		}
		
		public void setLeft(float left) {
			this.left = left;
		}
		
		public void setRight(float right) {
			this.right = right;
		}
		
		public void setTop(float top) {
			this.top = top;
		}
		
		public MutableEdgeInsets(float left, float top, float right, float bottom) {
			super(left, top, right, bottom);
		}
		
		public MutableEdgeInsets(float padding) {
			super(padding);
		}
		
		protected MutableEdgeInsets(@NonNull EdgeInsets source) {
			super(source);
		}
		
		@Override public @NonNull EdgeInsets copy() {
			return new EdgeInsets(this);
		}
		
		@Override public @NonNull MutableEdgeInsets mutableCopy() {
			try {
				return (MutableEdgeInsets)super.clone();
			} catch(CloneNotSupportedException exception) {
				return new MutableEdgeInsets(this);
			}
		}
	}
	
	public static class Point implements MutableCopying
	{
		protected float x;
		protected float y;
		
		public float getX() {
			return this.x;
		}
		
		public float getY() {
			return this.y;
		}
		
		public Point(float x, float y) {
			this.x = x;
			this.y = y;
		}
		
		protected Point(@NonNull Point source) {
			this(source.getX(), source.getY());
		}
		
		@Override public boolean equals(@Nullable Object obj) {
			if(obj == this) {
				return true;
			}
			
			if(!(obj instanceof Point)) {
				return false;
			}
			
			Point object = (Point)obj;
			
			if(Float.compare(this.x, object.x) != 0) {
				return false;
			}
			
			return (Float.compare(this.y, object.y) == 0);
		}
		
		@Override public int hashCode() {
			return Utilities.hashCode(this.x, this.y);
		}
		
		@Override public @NonNull String toString() {
			return String.format(Locale.US, "%s{%g, %g}", this.getClass().getSimpleName(), this.getX(), this.getY());
		}
		
		@Override public @NonNull Point copy() {
			return this;
		}
		
		@Override public @NonNull Object mutableCopy() {
			return new MutablePoint(this);
		}
	}
	
	public static class Point3D extends Point implements MutableCopying
	{
		protected float z;
		
		public float getZ() {
			return this.z;
		}
		
		public Point3D(float x, float y, float z) {
			super(x, y);
			
			this.z = z;
		}
		
		protected Point3D(@NonNull Point3D source) {
			this(source.getX(), source.getY(), source.getZ());
		}
		
		@Override public boolean equals(@Nullable Object obj) {
			if(obj == this) {
				return true;
			}
			
			if(!super.equals(obj) || !(obj instanceof Point3D)) {
				return false;
			}
			
			Point3D object = (Point3D)obj;
			
			return (Float.compare(this.z, object.z) == 0);
		}
		
		@Override public int hashCode() {
			return Utilities.hashCode(super.hashCode(), this.z);
		}
		
		@Override public @NonNull String toString() {
			return String.format(Locale.US, "%s{%g, %g, %g}", this.getClass().getSimpleName(), this.getX(), this.getY(), this.getZ());
		}
		
		@Override public @NonNull Point3D copy() {
			return this;
		}
		
		@Override public @NonNull Object mutableCopy() {
			return new MutablePoint3D(this);
		}
	}
	
	public static class MutablePoint extends Point
	{
		public void setX(float x) {
			this.x = x;
		}
		
		public void setY(float y) {
			this.y = y;
		}
		
		public MutablePoint(float x, float y) {
			super(x, y);
		}
		
		protected MutablePoint(@NonNull Point source) {
			super(source);
		}
		
		@Override public @NonNull Point copy() {
			return new Point(this);
		}
		
		@Override public @NonNull MutablePoint mutableCopy() {
			try {
				return (MutablePoint)super.clone();
			} catch(CloneNotSupportedException exception) {
				return new MutablePoint(this);
			}
		}
	}
	
	public static class MutablePoint3D extends Point3D
	{
		public void setX(float x) {
			this.x = x;
		}
		
		public void setY(float y) {
			this.y = y;
		}
		
		public void setZ(float z) {
			this.z = z;
		}
		
		public MutablePoint3D(float x, float y, float z) {
			super(x, y, z);
		}
		
		protected MutablePoint3D(@NonNull Point3D source) {
			super(source);
		}
		
		@Override public @NonNull Point3D copy() {
			return new Point3D(this);
		}
		
		@Override public @NonNull Object mutableCopy() {
			try {
				return super.clone();
			} catch(CloneNotSupportedException exception) {
				return new MutablePoint3D(this);
			}
		}
	}
	
	public static class Rect implements MutableCopying
	{
		protected @NonNull Point origin;
		protected @NonNull Size size;
		
		public @NonNull Point getBottomLeftVertex() {
			Point origin = this.getOrigin();
			return new Point(origin.getX(), origin.getY() + this.getSize().getHeight());
		}
		
		public @NonNull Point getBottomRightVertex() {
			Point origin = this.getOrigin();
			Size size = this.getSize();
			return new Point(origin.getX() + size.getWidth(), origin.getY() + size.getHeight());
		}
		
		public @NonNull Point getCenter() {
			Point origin = this.getOrigin();
			Size size = this.getSize();
			return new Point(origin.getX() + size.getWidth() / 2.0f, origin.getY() + size.getHeight() / 2.0f);
		}
		
		public @NonNull Point getOrigin() {
			return this.origin;
		}
		
		public @NonNull Size getSize() {
			return this.size;
		}
		
		public @NonNull Point getTopLeftVertex() {
			return this.getOrigin().copy();
		}
		
		public @NonNull Point getTopRightVertex() {
			Point origin = this.getOrigin();
			return new Point(origin.getX() + this.getSize().getWidth(), origin.getY());
		}
		
		public Rect(@NonNull Point origin, @NonNull Size size) {
			this.origin = origin;
			this.size = size;
		}
		
		protected Rect(@NonNull Rect source) {
			this(source.getOrigin(), source.getSize());
		}
		
		@Override public boolean equals(@Nullable Object obj) {
			if(obj == this) {
				return true;
			}
			
			if(!(obj instanceof Rect)) {
				return false;
			}
			
			Rect object = (Rect)obj;
			
			if(!Utilities.areObjectsEqual(this.origin, object.origin)) {
				return false;
			}
			
			return (Utilities.areObjectsEqual(this.size, object.size));
		}
		
		@Override public int hashCode() {
			return Utilities.hashCode(this.origin, this.size);
		}
		
		@Override public @NonNull String toString() {
			return String.format(Locale.US, "%s{%s, %s}", this.getClass().getSimpleName(), this.getOrigin(), this.getSize());
		}
		
		@Override public @NonNull Rect copy() {
			return this;
		}
		
		@Override public @NonNull MutableRect mutableCopy() {
			return new MutableRect(this);
		}
		
		public static @NonNull Rect inset(@NonNull Rect rect, float dx, float dy) {
			Point origin = rect.origin;
			float x = origin.x + dx;
			float y = origin.y + dy;
			
			Size size = rect.size;
			float w = size.width - 2 * dx;
			float h = size.height - 2 * dy;
			
			return new Rect(new Point(x, y), new Size(w, h));
		}
		
		public static @Nullable Rect getIntersection(@Nullable Rect first, @Nullable Rect second) {
			if((first == null) || (second == null)) {
				return null;
			}
			
			if(first.equals(second)) {
				return first;
			}
			
			Point origin = first.getOrigin();
			float fx1 = origin.getX();
			float fy1 = origin.getY();
			
			Size size = first.getSize();
			float fx2 = fx1 + size.getWidth();
			float fy2 = fy1 + size.getHeight();
			
			origin = second.getOrigin();
			float sx1 = origin.getX();
			float sy1 = origin.getY();
			
			size = second.getSize();
			float sx2 = fx1 + size.getWidth();
			float sy2 = fy1 + size.getHeight();
			
			float rx1 = Math.max(fx1, sx1);
			float rx2 = Math.min(fx2, sx2);
			
			if(Float.compare(rx1, rx2) >= 0) {
				return null;
			}
			
			float ry1 = Math.max(fy1, sy1);
			float ry2 = Math.min(fy2, sy2);
			
			if(Float.compare(ry1, ry2) >= 0) {
				return null;
			}
			
			return new Rect(new Point(rx1, ry1), new Size((rx2 - rx1), (ry2 - ry1)));
		}
	}
	
	public static class MutableRect extends Rect
	{
		public void setOrigin(@NonNull Point origin) {
			this.origin = origin;
		}
		
		public void setSize(@NonNull Size size) {
			this.size = size;
		}
		
		public MutableRect(@NonNull Point origin, @NonNull Size size) {
			super(origin, size);
		}
		
		protected MutableRect(@NonNull Rect source) {
			super(source);
		}
		
		@Override public @NonNull Rect copy() {
			return new Rect(this);
		}
		
		@Override public @NonNull MutableRect mutableCopy() {
			try {
				return (MutableRect)super.clone();
			} catch(CloneNotSupportedException exception) {
				return new MutableRect(this);
			}
		}
	}
	
	public static class Size implements MutableCopying
	{
		protected float height;
		protected float width;
		
		public float getHeight() {
			return this.height;
		}
		
		public float getWidth() {
			return this.width;
		}
		
		public Size(float width, float height) {
			this.width = width;
			this.height = height;
		}
		
		protected Size(@NonNull Size source) {
			this(source.getWidth(), source.getHeight());
		}
		
		@Override public boolean equals(@Nullable Object obj) {
			if(obj == this) {
				return true;
			}
			
			if(!(obj instanceof Size)) {
				return false;
			}
			
			Size object = (Size)obj;
			
			if(Float.compare(this.width, object.width) != 0) {
				return false;
			}
			
			return (Float.compare(this.height, object.height) == 0);
		}
		
		@Override public int hashCode() {
			return Utilities.hashCode(this.height, this.width);
		}
		
		@Override public @NonNull String toString() {
			return String.format(Locale.US, "%s{%g, %g}", this.getClass().getSimpleName(), this.getWidth(), this.getHeight());
		}
		
		@Override public @NonNull Size copy() {
			return this;
		}
		
		@Override public @NonNull MutableSize mutableCopy() {
			return new MutableSize(this);
		}
	}
	
	public static class MutableSize extends Size
	{
		public void setHeight(float height) {
			this.height = height;
		}
		
		public void setWidth(float width) {
			this.width = width;
		}
		
		public MutableSize(float width, float height) {
			super(width, height);
		}
		
		protected MutableSize(@NonNull Size source) {
			super(source);
		}
		
		@Override public @NonNull Size copy() {
			return new Size(this);
		}
		
		@Override public @NonNull MutableSize mutableCopy() {
			try {
				return (MutableSize)super.clone();
			} catch(CloneNotSupportedException exception) {
				return new MutableSize(this);
			}
		}
	}
	
	public static final @NonNull EdgeInsets EDGE_INSETS_ZERO = new EdgeInsets(0, 0, 0, 0);
	
	public static final @NonNull Point POINT_ZERO = new Point(0, 0);
	public static final @NonNull Size SIZE_ZERO = new Size(0, 0);
	
	public static final @NonNull Rect RECT_ZERO = new Rect(POINT_ZERO, SIZE_ZERO);
}

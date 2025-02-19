/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.common.observable;

import bisq.common.observable.Observable;
import bisq.common.observable.ObservableSet;
import bisq.common.observable.Pin;
import bisq.desktop.common.threading.UIThread;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.function.Consumer;
import java.util.function.Function;

public class FxBindings {
    public static <T, R> ObservableListBindings<T, R> bind(ObservableList<R> observer) {
        return new ObservableListBindings<>(observer);
    }

    public static <T> ObservablePropertyBindings<T> bind(ObjectProperty<T> observer) {
        return new ObservablePropertyBindings<>(observer);
    }

    public static LongPropertyBindings bind(LongProperty observer) {
        return new LongPropertyBindings(observer);
    }

    public static DoublePropertyBindings bind(DoubleProperty observer) {
        return new DoublePropertyBindings(observer);
    }

    public static IntegerPropertyBindings bind(IntegerProperty observer) {
        return new IntegerPropertyBindings(observer);
    }

    public static BooleanPropertyBindings bind(BooleanProperty observer) {
        return new BooleanPropertyBindings(observer);
    }

    public static StringPropertyBindings bind(StringProperty observer) {
        return new StringPropertyBindings(observer);
    }

    public static <T> Pin subscribe(Observable<T> observable, Consumer<T> consumer) {
        return observable.addObserver(e -> UIThread.run(() -> consumer.accept(e)));
    }

    public static class ObservableListBindings<T, R> {
        private final ObservableList<R> observer;
        @SuppressWarnings("unchecked")
        private Function<T, R> mapFunction = e -> (R) e;

        private ObservableListBindings(ObservableList<R> observer) {
            this.observer = observer;
        }

        public ObservableListBindings<T, R> map(Function<T, R> mapper) {
            this.mapFunction = mapper;
            return this;
        }

        public Pin to(ObservableSet<T> observable) {
            return observable.addObserver(observer, mapFunction, UIThread::run);
        }
    }

    public record ObservablePropertyBindings<T>(ObjectProperty<T> observer) {
        public Pin to(Observable<T> observable) {
            return observable.addObserver(e -> UIThread.run(() -> observer.set(e)));
        }
    }

    public record LongPropertyBindings(LongProperty observer) {
        public Pin to(Observable<Long> observable) {
            return observable.addObserver(e -> UIThread.run(() -> observer.set(e)));
        }
    }

    public record DoublePropertyBindings(DoubleProperty observer) {
        public Pin to(Observable<Double> observable) {
            return observable.addObserver(e -> UIThread.run(() -> observer.set(e)));
        }
    }

    public record IntegerPropertyBindings(IntegerProperty observer) {
        public Pin to(Observable<Integer> observable) {
            return observable.addObserver(e -> UIThread.run(() -> observer.set(e)));
        }
    }

    public record BooleanPropertyBindings(BooleanProperty observer) {
        public Pin to(Observable<Boolean> observable) {
            return observable.addObserver(e -> UIThread.run(() -> observer.set(e)));
        }
    }

    public record StringPropertyBindings(StringProperty observer) {
        public Pin to(Observable<String> observable) {
            return observable.addObserver(e -> UIThread.run(() -> observer.set(e)));
        }
    }
}
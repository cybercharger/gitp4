package gitp4;

/**
 * Created by ChrisKang on 7/5/2017.
 */
@FunctionalInterface
public interface ThrowableConsumer<T, E extends Exception> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t) throws E;
}

package com.iv.logView.ui;

import javax.swing.SwingUtilities;

/**
 * This is the 3rd version of SwingWorker (also known as
 * SwingWorker 3), an abstract class that you subclass to
 * perform GUI-related work in a dedicated thread.  For
 * instructions on using this class, see:
 * <p/>
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 * <p/>
 * Note that the API changed slightly in the 3rd version:
 * You must now invoke start() on the SwingWorker after
 * creating it.
 */
public abstract class SwingWorker<T> {

    /**
     * see getValue(), setValue()
     */
    private T value;

    private final ThreadVar threadVar;

    /**
     * Start a thread that will call the <code>construct</code> method
     * and then exit.
     */
    public SwingWorker() {
        final Runnable doFinished = new Runnable() {
            public void run() {
                finished();
            }
        };

        Runnable doConstruct = new Runnable() {
            public void run() {
                try {
                    setValue(construct());
                }
                finally {
                    threadVar.clear();
                    SwingUtilities.invokeLater(doFinished);
                }
            }
        };

        Thread thread = new Thread(doConstruct);
        threadVar = new ThreadVar(thread);
    }

    /**
     * Class to maintain reference to current worker thread
     * under separate synchronization control.
     */
    private static class ThreadVar {
        private Thread thread;

        ThreadVar(Thread thread) {
            this.thread = thread;
        }

        synchronized Thread get() {
            return thread;
        }

        synchronized void clear() {
            thread = null;
        }
    }

    /**
     * Get the value produced by the worker thread, or null if it
     * hasn't been constructed yet.
     *
     * @return value
     */
    protected synchronized T getValue() {
        return value;
    }

    /**
     * Set the value produced by worker thread
     * @param value value produced by worker thread
     */
    private synchronized void setValue(T value) {
        this.value = value;
    }

    /**
     * Compute the value to be returned by the <code>get</code> method.
     * @return result of execution
     */
    public abstract T construct();

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    public void finished() {
    }

    /**
     * A new method that interrupts the worker thread.  Call this method
     * to force the worker to stop what it's doing.
     */
    public void interrupt() {
        Thread thread = threadVar.get();
        if (thread != null) {
            thread.interrupt();
        }
        threadVar.clear();
    }

    /**
     * Return the value created by the <code>construct</code> method.
     * Returns null if either the constructing thread or the current
     * thread was interrupted before a value was produced.
     *
     * @return the value created by the <code>construct</code> method
     */
    public T get() {
        for (;;) {
            Thread thread = threadVar.get();
            if (thread == null) {
                return getValue();
            }
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }

    /**
     * Start the worker thread.
     */
    public void start() {
        Thread thread = threadVar.get();
        if (thread != null) {
            thread.start();
        }
    }

}

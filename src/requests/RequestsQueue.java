package requests;

import java.util.LinkedList;

public class RequestsQueue<T> {
    private final LinkedList<T> requests = new LinkedList<>();
    private boolean isEnd = false;

    public synchronized void rawNotify() {
        this.notify();
    }

    public synchronized void addRequest(T request) {
        requests.add(request);
        // If it is the waitQueue, the only one thread is Scheduler
        // If it is the passageRequestsQueue, the only one thread is Elevator
        this.notifyAll();
    }

    public synchronized void setEnd() {
        this.isEnd = true;
        // Not frequently called, so notifyAll is acceptable
        this.notifyAll();
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        return requests.isEmpty();
    }

    public synchronized T popRequest() {
        if (requests.isEmpty() && !isEnd) {
            try {
                this.wait();  // Wait for addRequest
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
        }
        if (requests.isEmpty()) {
            return null;
        }
        T request = requests.get(0);
        requests.remove(0);
        this.notify();
        return request;
    }

    public synchronized T popRequestWithoutWait() {
        // System.out.println("popRequestWithoutWait");
        if (requests.isEmpty()) {
            return null;
        }
        T request = requests.get(0);
        requests.remove(0);
        // System.out.println("popRequestWithoutWait" + request);
        this.notify();
        return request;
    }

    public LinkedList<T> dangerousGetRequests() {
        return requests;
    }
}

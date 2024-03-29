package requests;

import java.util.LinkedList;

public class PassageRequestsQueue {
    private final LinkedList<PassageRequest> requests = new LinkedList<>();
    private boolean isEnd = false;

    public synchronized void addRequest(PassageRequest request) {
        requests.add(request);
        // If it is the waitQueue, the only one thread is Scheduler
        // If it is the passageRequestsQueue, the only one thread is Elevator
        this.notify();
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

    public synchronized PassageRequest popRequest() {
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
        PassageRequest request = requests.get(0);
        requests.remove(0);
        this.notify();
        return request;
    }

    public synchronized PassageRequest popRequestWithoutWait() {
        // System.out.println("popRequestWithoutWait");
        if (requests.isEmpty()) {
            return null;
        }
        PassageRequest request = requests.get(0);
        requests.remove(0);
        // System.out.println("popRequestWithoutWait" + request);
        this.notify();
        return request;
    }
}

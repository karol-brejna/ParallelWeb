package pl.atena.technoblog.parallel;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/parallel2")
public class Parallel2 extends HttpServlet {
	private static final long serialVersionUID = -152801880710512087L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// get the number of workers to run
		int count = this.getRequestedCount(request, 4);
		// get the executor service
		final ServletContext sc = this.getServletContext();
		final ExecutorService executorService = (ExecutorService) sc
				.getAttribute("myExecutorService");
		// create work coordinator
		CountDownLatch countDownLatch = new CountDownLatch(count);
		
		Long t1 = System.nanoTime();
		// create the workers
		List<RunnableWorkUnit2> workers = new ArrayList<RunnableWorkUnit2>();
		for (int i = 0; i < count; i++) {
			RunnableWorkUnit2 wu = new RunnableWorkUnit2("RunnableTask"
					+ String.valueOf(i + 1), countDownLatch);
			workers.add(wu);
		}
		// run the workers through the executor
		for (RunnableWorkUnit2 wu : workers) {
			executorService.execute(wu);
		}

		try {
			System.out.println("START WAITING");
			countDownLatch.await();
			System.out.println("DONE WAITING");
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		
		Long t2 = System.nanoTime();
		Writer w = response.getWriter();
		w.write(String.format("\n Request processed in %dms!", (t2 - t1) / 1000000));
		w.flush();
		w.close();
	}
	/** 
	 * Method for getting the requested workers' count. 
	 * If no such param in the request, assign defaultValue. 
	 */
	private int getRequestedCount(HttpServletRequest request, int defaultValue) {
		int result = defaultValue;
		String param = request.getParameter("count");
		if (param != null) {
			result = Integer.parseInt(param);
		}
		return result;
	}
}
package com.ctrl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import com.multicast.MulticastRx;

public class Action {

	public String getSource() {
		// TODO Auto-generated method stub
		return new SourceAndPort().getSource();
	}

	public int getPort() {
		// TODO Auto-generated method stub
		return new SourceAndPort().getPort();
	}

	public void routing(MulticastRx mrx, Vector<String> path, String srch) {
		// TODO Auto-generated method stub
		try {
			Set<String> set = mrx.hm.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				String[] data = key.split(",");
				String nnode = data[0];
				String nsys = data[2];
				int nPort = Integer.parseInt(data[3]);
				if (getAvailable(path, nnode)) {
					Socket socket = new Socket(nsys, nPort);
					ObjectOutputStream oos = new ObjectOutputStream(socket
							.getOutputStream());
					oos.writeObject("SREQ");
					oos.writeObject(path);
					oos.writeObject(srch);
				}

			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean getAvailable(Vector<String> path, String nei) {
		// TODO Auto-generated method stub
		return !path.contains(nei);
	}

	public void sendData(MulticastRx mrx, Vector<String> path,
			Vector<String> retpath, String text) {
		// TODO Auto-generated method stub
		try {
			path.remove(0);
			String neigh = path.get(0);
			Set<String> set = mrx.hm.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				String[] data = key.split(",");
				String nnode = data[0];
				String nsys = data[2];
				int nport = Integer.parseInt(data[3]);
				if (neigh.equals(nnode)) {
					Socket socket = new Socket(nsys, nport);
					ObjectOutputStream oos = new ObjectOutputStream(socket
							.getOutputStream());
					oos.writeObject("DATA");
					oos.writeObject(path);
					oos.writeObject(retpath);
					oos.writeObject(text);
					break;
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Vector<String> getPath(String mpcrPath) {
		// TODO Auto-generated method stub
		Vector<String> vec = new Vector<String>();
		StringTokenizer st = new StringTokenizer(mpcrPath, ",");
		while (st.hasMoreElements()) {
			String object = (String) st.nextElement();
			vec.add(object);
		}
		return vec;
	}

	public void disconnect(Vector<String> path, String peer,
			HashMap<String, Long> hm) {
		// TODO Auto-generated method stub
		try {
			Set<String> set = hm.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				String[] data = key.split(",");
				String nnode = data[0];
				String nsys = data[2];
				int nPort = Integer.parseInt(data[3]);
				if (getAvailable(path, nnode)) {
					Socket socket = new Socket(nsys, nPort);
					ObjectOutputStream oos = new ObjectOutputStream(socket
							.getOutputStream());
					oos.writeObject("DISCONNECT");
					oos.writeObject(path);
					oos.writeObject(peer);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}

	}

	public void sendDisData(MulticastRx mrx, Vector<String> path,
			Vector<String> retpath, String text, Integer ino) {
		// TODO Auto-generated method stub
		try {
			path.remove(0);
			String neigh = path.get(0);
			Set<String> set = mrx.hm.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				String[] data = key.split(",");
				String nnode = data[0];
				String nsys = data[2];
				int nport = Integer.parseInt(data[3]);
				if (neigh.equals(nnode)) {
					Socket socket = new Socket(nsys, nport);
					ObjectOutputStream oos = new ObjectOutputStream(socket
							.getOutputStream());
					oos.writeObject("MISDATA");
					oos.writeObject(path);
					oos.writeObject(retpath);
					oos.writeObject(text);
					oos.writeObject(ino);
					break;
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

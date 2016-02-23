package com.multicast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.design.Server;

public class MulticastRx extends Thread {
	private InetAddress ia;
	public HashMap<String, Long> hm = new HashMap<String, Long>();
	int dis;
	Server peer;

	public MulticastRx(Server peer) {
		this.peer = peer;
		dis = Integer.parseInt(peer.dis);
		start();
		availability();
	}

	private void availability() {
		// TODO Auto-generated method stub
		new Thread(this) {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				while (true) {
					try {
						Date date = new Date();
						long pre = date.getTime();
						Thread.sleep(1000);
						Set<String> set = hm.keySet();
						Iterator<String> it = set.iterator();
						while (it.hasNext()) {
							String key = (String) it.next();
							long time = hm.get(key);
							if (time < pre) {
								hm.remove(key);
								refresh(hm);
								Vector<String> path = new Vector<String>();
								path.add(peer.source);
								peer.action.disconnect(path, getDPeer(key), hm);
								break;
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println(e.getMessage());
						continue;
					}
				}
			}

			private String getDPeer(String key) {
				// TODO Auto-generated method stub
				String[] str = key.split(",");
				return str[0];
			}

		}.start();
	}

	private void refresh(HashMap<String, Long> hm) {
		// TODO Auto-generated method stub
		peer.jtaNeigh.setText("");
		Set<String> set = hm.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String[] data = key.split(",");
			String nnode = data[0];
			String nsys = data[2];
			peer.jtaNeigh.append(nnode + " " + nsys + "\n");

		}
	}

	private void print(String str) {
		// TODO Auto-generated method stub
		if (hm.containsKey(str)) {
			hm.put(str, new Date().getTime());
		} else {
			String[] data = str.split(",");
			String nnode = data[0];
			int ndis = Integer.parseInt(data[1]);
			String nsys = data[2];
			int min = dis - 10;
			int max = dis + 10;
			if ((!nnode.equals(peer.source)) && ndis >= min && ndis <= max) {
				peer.jtaNeigh.append(nnode + " " + nsys + "\n");
				hm.put(str, new Date().getTime());
				// System.out.println(hm);
			}
		}

	}

	public void run() {
		try {
			while (true) {
				ia = InetAddress.getByName("228.5.6.7");
				MulticastSocket ms = new MulticastSocket(4444);
				ms.joinGroup(ia);
				byte[] b = new byte[512];
				DatagramPacket dp = new DatagramPacket(b, b.length);
				ms.receive(dp);
				String str = new String(dp.getData());
				str = str.trim();
				print(str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
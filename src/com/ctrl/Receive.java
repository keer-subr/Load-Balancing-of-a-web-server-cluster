package com.ctrl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.design.Server;

public class Receive extends Thread {

	private Socket soc;
	private ServerSocket serSoc;
	private ObjectInputStream ois;
	int port;
	Server peer;
	Action action;
	boolean flag;
	public TreeMap<String, String> allPaths = new TreeMap<String, String>();
	Integer ino;

	public Receive(Server peer, int port, Action action) {
		this.peer = peer;
		this.port = port;
		this.action = action;
		start();
	}

	public void run() {
		try {
			receive();
		} catch (Exception e) {
		}
	}

	public void receive() {
		try {
			serSoc = new ServerSocket(port);
			while (true) {
				soc = serSoc.accept();
				ois = new ObjectInputStream(soc.getInputStream());
				String str = (String) ois.readObject();
				checkStatus(str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String fdata = "";

	private void checkStatus(String str) {
		try {
			if (str.equals("SREQ")) {
				Vector<String> path = (Vector<String>) ois.readObject();
				String dest = (String) ois.readObject();
				if (peer.allFiles.contains(dest)) {
					path.add(peer.source);
					Vector<String> retvec = new Vector<String>();
					for (int i = 0; i < path.size(); i++) {
						retvec.add(path.get(i));
					}
					retvec.remove(retvec.size() - 1);
					sendCTS(dest, path, retvec, peer.source);

				}
				path.add(peer.source);
				peer.action.routing(peer.mrx, path, dest);

			} else if (str.equals("SREP")) {
				String file = (String) ois.readObject();
				Vector<String> path = (Vector<String>) ois.readObject();
				Vector<String> retvec = (Vector<String>) ois.readObject();
				String dsrc = (String) ois.readObject();
				String fPath = "";
				if (retvec.get(0).equals(peer.source)) {
					for (int i = 0; i < path.size(); i++) {
						if (i < path.size() - 1)
							fPath += path.get(i) + ",";
						else
							fPath += path.get(i);
					}

					if (!allPaths.containsKey(dsrc)) {
						allPaths.put(dsrc, fPath);
						peer.jtaSarchDet.append("Server Name: " + dsrc + "\n");
						peer.jtaSarchDet.append("File Name: " + file + "\n");
						peer.jtaSarchDet
								.append("Random Walk : " + fPath + "\n");
						peer.jtaSarchDet
								.append("------------------------------" + "\n");
					}
					stop = true;
				} else {
					retvec.remove(retvec.size() - 1);
					sendCTS(file, path, retvec, dsrc);
				}

			} else if (str.equals("DATA")) {
				Vector<String> path = (Vector<String>) ois.readObject();
				Vector<String> retpath = (Vector<String>) ois.readObject();
				String text = (String) ois.readObject();
				if (peer.source.equals(path.get(path.size() - 1))) {
					Vector<String> retvec = new Vector<String>();
					for (int i = 0; i < retpath.size(); i++) {
						retvec.add(retpath.get(i));
					}
					retpath.remove(retpath.size() - 1);
					response(text, retpath, retvec);
				} else
					action.sendData(peer.mrx, path, retpath, text);
			} else if (str.equals("MISDATA")) {
				Vector<String> path = (Vector<String>) ois.readObject();
				Vector<String> retpath = (Vector<String>) ois.readObject();
				String text = (String) ois.readObject();
				Integer ino = (Integer) ois.readObject();
				if (peer.source.equals(path.get(path.size() - 1))) {
					Vector<String> retvec = new Vector<String>();
					for (int i = 0; i < retpath.size(); i++) {
						retvec.add(retpath.get(i));
					}
					retpath.remove(retpath.size() - 1);
					response(text, retpath, retvec, ino);
				} else
					action.sendDisData(peer.mrx, path, retpath, text, ino);
			} else if (str.equals("PAC")) {
				ino = (Integer) ois.readObject();
				String pac = (String) ois.readObject();
				Vector<String> retvec = (Vector<String>) ois.readObject();
				Vector<String> pacpath = (Vector<String>) ois.readObject();
				if (retvec.get(0).equals(peer.source)) {
					peer.dftDown.addRow(new Object[] { "Packet " + (ino + 1),
							pac, pacpath });
					fdata += pac + " ";

				} else {
					retvec.remove(retvec.size() - 1);
					sendRes(ino, pac, retvec, pacpath);
				}
			} else if (str.equals("ST")) {
				Vector<String> retvec = (Vector<String>) ois.readObject();
				if (retvec.get(0).equals(peer.source)) {
					flag = true;
					System.out.println("Start.");
				} else {
					retvec.remove(retvec.size() - 1);
					sendStart("ST", retvec);
				}
			} else if (str.equals("END")) {
				Vector<String> retvec = (Vector<String>) ois.readObject();
				if (retvec.get(0).equals(peer.source)) {
					flag = false;
					File file = new File("Received/" + peer.txtSearch.getText());
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(fdata.getBytes());
					fos.close();
					JOptionPane.showMessageDialog(null,
							"Received File saved at : "
									+ file.getAbsolutePath());
					System.out.println("End.");
				} else {
					retvec.remove(retvec.size() - 1);
					sendStart("END", retvec);
				}
			} else if (str.equals("DISCONNECT")) {
				if (stop) {
					Thread.sleep(100);
					Vector<String> retvec = (Vector<String>) ois.readObject();
					String prs = (String) ois.readObject();
					if (flag) {
						Set<String> set = allPaths.keySet();
						Iterator<String> it = set.iterator();
						while (it.hasNext()) {
							String ky = (String) it.next();
							if (!ky.equals(prs)) {
								String ph = allPaths.get(ky);
								Vector<String> path = action.getPath(ph);
								Vector<String> retpath = action.getPath(ph);
								peer.action.sendDisData(peer.mrx, path,
										retpath, peer.txtSearch.getText(),
										++ino);
								stop = false;
								break;
							}
						}

					} else {
						retvec.add(peer.source);
						peer.action.disconnect(retvec, prs, peer.mrx.hm);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean stop = true;

	private void response(String text, Vector<String> retpath,
			Vector<String> pacpath, Integer ino2) {
		// TODO Auto-generated method stub
		try {
			File file = new File("Database\\" + text);
			FileInputStream fis = new FileInputStream(file);
			byte[] b = new byte[fis.available()];
			fis.read(b);
			fis.close();
			String[] str = new String(b).split(" ");
			boolean as = false;
			for (int i = 0; i < str.length; i++) {
				if (i == ino2)
					as = true;
				if (as) {
					sendRes(i, str[i], retpath, pacpath);
					peer.dftDataTx.addRow(new Object[] { pacpath.get(0),
							"Packet : " + i + " :" + text, str[i] });
					Thread.sleep(1000);
				}
			}
			sendStart("END", retpath);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sendRes(Integer ino, String pac, Vector<String> retvec,
			Vector<String> pacpath) {
		// TODO Auto-generated method stub
		try {
			String neigh = retvec.get(retvec.size() - 1);
			Set<String> set = peer.mrx.hm.keySet();
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
					oos.writeObject("PAC");
					oos.writeObject(ino);
					oos.writeObject(pac);
					oos.writeObject(retvec);
					oos.writeObject(pacpath);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void response(String text, Vector<String> retpath,
			Vector<String> pacpath) {
		// TODO Auto-generated method stub
		try {
			File file = new File("Database\\" + text);
			FileInputStream fis = new FileInputStream(file);
			byte[] b = new byte[fis.available()];
			fis.read(b);
			fis.close();
			String[] str = new String(b).split(" ");
			System.out.println("ST RETP:" + retpath);
			sendStart("ST", retpath);
			System.out.println("PEC RETP:" + retpath);
			for (int i = 0; i < str.length; i++) {
				sendRes(i, str[i], retpath, pacpath);
				peer.dftDataTx.addRow(new Object[] { pacpath.get(0),
						"Packet : " + i + " :" + text, str[i] });
				Thread.sleep(1000);
			}
			System.out.println("END RETP:" + retpath);
			sendStart("END", retpath);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendStart(String string, Vector<String> retvec) {
		// TODO Auto-generated method stub
		try {
			String neigh = retvec.get(retvec.size() - 1);
			Set<String> set = peer.mrx.hm.keySet();
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
					oos.writeObject(string);
					oos.writeObject(retvec);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendCTS(String file, Vector<String> path,
			Vector<String> retvec, String dsrc) {
		// TODO Auto-generated method stub
		try {
			String neigh = retvec.get(retvec.size() - 1);
			Set<String> set = peer.mrx.hm.keySet();
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
					oos.writeObject("SREP");
					oos.writeObject(file);
					oos.writeObject(path);
					oos.writeObject(retvec);
					oos.writeObject(dsrc);

				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

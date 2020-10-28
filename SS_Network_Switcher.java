/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ss_network_switcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.zone.ZoneOffsetTransitionRule;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author luc
 */
public class SS_Network_Switcher {

    private String propertyLocation;
    private Properties property;
    private File net1, net2, current;
    private String net1_info, net2_info, current_info;

    public void setNet1(File net1) {
        this.net1 = net1;
    }

    public void setNet2(File net2) {
        this.net2 = net2;
    }

    public void setCurrent(File current) {
        this.current = current;
    }

    private void loadNetworkInformation() {
        current_info = read(current);
        net1_info = read(net1);
        net2_info = read(net2);

    }

    private void switchNetwork() {
        if (current_info.trim().equals(net1_info.trim())) {
            write(net2_info);
            System.out.println(LocalDate.now().toString() + " " + Time.valueOf(LocalTime.now()).toString() + ": Switched to network 2");
            applyNetplan();
        } else if (current_info.trim().equals(net2_info.trim())) {
            write(net1_info);
            System.out.println(LocalDate.now().toString() + " " + Time.valueOf(LocalTime.now()).toString() + ": Switched to network 1");
            applyNetplan();
        }

    }

    private void applyNetplan() {
        try {
            Process proc = Runtime.getRuntime().exec("sudo netplan apply");
            proc.waitFor();
            System.out.println(LocalDate.now().toString() + " " + Time.valueOf(LocalTime.now()).toString() + ": Saving settings");
        } catch (IOException ex) {
            Logger.getLogger(SS_Network_Switcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SS_Network_Switcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String read(File f) {
        StringBuilder sb = new StringBuilder();
        try {
            FileReader reader = new FileReader(f);
            BufferedReader bf = new BufferedReader(reader);
            while (true) {
                String line = bf.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line + "\n");
            }
            bf.close();
            reader.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SS_Network_Switcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SS_Network_Switcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

    public void write(String text) {
        PrintWriter out = null;
        BufferedWriter bufferedWriter;
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(current);
            bufferedWriter = new BufferedWriter(fileWriter);
            out = new PrintWriter(bufferedWriter);
            out.write(text);
            out.println();
            out.flush();
            out.close();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {

        }

    }

    private boolean isConnected() {
        try {
            Process proc = Runtime.getRuntime().exec("dig +short myip.opendns.com @resolver1.opendns.com");

            BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }

                output.append(line);
            }
            proc.waitFor();
            String outputSt = output.toString().replaceAll("\\.", " . ");
            String[] ts = outputSt.split(" . ");
            if (ts.length == 4) {
                try {
                    Integer.parseInt(ts[0]);
                    Integer.parseInt(ts[0]);
                    Integer.parseInt(ts[1]);
                    Integer.parseInt(ts[2]);
                    Integer.parseInt(ts[3]);
                    return true;
                } catch (NumberFormatException d) {
                    return false;
                }

            }
        } catch (IOException v) {

        } catch (InterruptedException ex) {
            Logger.getLogger(SS_Network_Switcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Version 1.0");
        if (args.length < 3) {
            System.err.println(LocalDate.now().toString() + " " + Time.valueOf(LocalTime.now()).toString() + ": Wrong number of args.\nRequired current netplan file, network v1, network v2");
            System.exit(0);
        }
        SS_Network_Switcher s = new SS_Network_Switcher();
        if (!s.isConnected()) {

            File current = new File(args[0]);
            File net1 = new File(args[1]);
            File net2 = new File(args[2]);
            if (current.isFile() && net1.isFile() && net2.isFile()) {

                s.setCurrent(current);
                s.setNet1(net1);
                s.setNet2(net2);
                s.loadNetworkInformation();
                s.switchNetwork();
            } else {
                System.err.println(LocalDate.now().toString() + ": All three args need to be file paths");
                System.exit(0);
            }
        } else {
            System.out.println(LocalDate.now().toString() + " " + Time.valueOf(LocalTime.now()).toString() + ":  No Changes needed");
        }

    }

}

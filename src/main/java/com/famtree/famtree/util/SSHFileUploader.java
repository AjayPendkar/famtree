package com.famtree.famtree.util;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

@Component
public class SSHFileUploader {

    @Value("${ssh.host:46.202.161.144}")
    private String sshHost;

    @Value("${ssh.port:65002}")
    private int sshPort;

    @Value("${ssh.user:u710971409}")
    private String sshUser;

    @Value("${ssh.password:Msskmsksmk2@}")
    private String sshPassword;

    @Value("${ssh.remote.dir:/home/u710971409/public_html/uploads/}")
    private String remoteDir;

    @Value("${ssh.base.url:https://yourdomain.com/uploads/}")
    private String baseUrl;

    public String uploadFile(String localFilePath, String remoteFileName) {
        Session session = null;
        Channel channel = null;
        ChannelExec channelExec = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(sshUser, sshHost, sshPort);
            session.setPassword(sshPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            boolean isDirExist = checkAndCreateDirectory(session, remoteDir);
            if (!isDirExist) {
                throw new RuntimeException("Failed to create remote directory!");
            }

            channel = session.openChannel("exec");
            channelExec = (ChannelExec) channel;

            String command = "scp -t " + remoteDir + remoteFileName;
            channelExec.setCommand(command);

            OutputStream out = channelExec.getOutputStream();
            channelExec.connect();

            File file = new File(localFilePath);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];

            while (fis.read(buffer) > 0) {
                out.write(buffer);
            }
            out.flush();
            fis.close();
            out.close();

            return baseUrl + remoteFileName;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading file via SSH: " + e.getMessage(), e);
        } finally {
            if (channelExec != null) channelExec.disconnect();
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    private boolean checkAndCreateDirectory(Session session, String directory) {
        try {
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand("mkdir -p " + directory);
            channelExec.connect();
            channelExec.disconnect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 
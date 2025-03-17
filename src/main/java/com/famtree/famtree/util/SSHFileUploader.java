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

    @Value("${ssh.password:Mskmskmsk2@}")
    private String sshPassword;

    @Value("${ssh.remote.dir:/home/u710971409/domains/purify.fit/public_html/uploads/}")
    private String remoteDir;

    @Value("${ssh.base.url:https://purify.fit/uploads/}")
    private String baseUrl;

    public String uploadFile(String localFilePath, String remoteFileName) {
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(sshUser, sshHost, sshPort);
            session.setPassword(sshPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            // Create directory if it doesn't exist and set permissions
            try {
                try {
                    sftpChannel.mkdir(remoteDir);
                } catch (SftpException e) {
                    // Directory might already exist, ignore the error
                }
                // Set directory permissions to 755 (rwxr-xr-x)
                sftpChannel.chmod(0755, remoteDir);
            } catch (SftpException e) {
                // Log error but continue
                System.err.println("Warning: Could not set directory permissions: " + e.getMessage());
            }

            // Upload the file
            File file = new File(localFilePath);
            FileInputStream fis = new FileInputStream(file);
            sftpChannel.put(fis, remoteDir + remoteFileName);
            fis.close();

            try {
                // Set file permissions to 644 (rw-r--r--)
                sftpChannel.chmod(0644, remoteDir + remoteFileName);
            } catch (SftpException e) {
                // Log error but continue
                System.err.println("Warning: Could not set file permissions: " + e.getMessage());
            }

            // Execute a command to ensure proper ownership
            Channel execChannel = session.openChannel("exec");
            ((ChannelExec) execChannel).setCommand(
                "chown " + sshUser + ":" + sshUser + " " + remoteDir + remoteFileName
            );
            execChannel.connect();
            execChannel.disconnect();

            return baseUrl + remoteFileName;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading file via SSH: " + e.getMessage(), e);
        } finally {
            if (sftpChannel != null) sftpChannel.disconnect();
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
package com.itt.tds.node;

import com.itt.tds.client.InvalidCommandException;
import com.itt.tds.core.Constants;
import com.itt.tds.core.Networking.RequestSender;
import com.itt.tds.core.Networking.TDSRequest;
import com.itt.tds.core.Networking.TDSResponse;
import com.itt.tds.core.model.TDSDistributorConfiguration;
import com.itt.tds.core.enums.NodeStatus;
import com.itt.tds.core.enums.ResponseStatus;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author nitin.jangid
 */
public class RegisterCommandExecutor implements CommandExecutor {

    @Override
    public void executeCommand(String[] parameters) throws InvalidCommandException {
        String hostName;
        String ipAddress;

        if (parameters.length == 0) {
            TDSDistributorConfiguration configuration = new TDSDistributorConfigurationFileProcessor().read();
            if (Utils.isDistributorConfigured(configuration)) {
                if (!Utils.isNodeRegistered()) {
                    try {
                        InetAddress inetAddress = InetAddress.getLocalHost();
                        hostName = inetAddress.getHostName();
                        ipAddress = inetAddress.getHostAddress();
                    } catch (UnknownHostException ex) {
                        Utils.showMessage("Not able to retrive the IP address of the system");
                        return;
                    }

                    try {
                        TDSRequest tdsRequest = new TDSRequest();
                        tdsRequest.setMethod(Constants.NODE_REGISTER);
                        tdsRequest.setDestinationPort(configuration.getDistributorPortNumber());
                        tdsRequest.setDestinationIp(configuration.getDistributorIpAddress());
                        tdsRequest.setParameter(Constants.HOST_NAME, hostName);
                        tdsRequest.setParameter(Constants.PORT_NUMBER, 50);
                        tdsRequest.setParameter(Constants.STATUS, NodeStatus.NOT_OPERATIONAL.name());
                        tdsRequest.setParameter(Constants.IP_ADDRESS, ipAddress);
                        TDSResponse response = RequestSender.sendRequest(tdsRequest);
                        if (response.getStatus() == ResponseStatus.OK.getValue()) {
                            new NodeIdFileProcessor().write((String) response.getValue(Constants.NODE_ID));
                            Utils.showMessage("Node is registered successfully with ID : " + response.getValue(Constants.NODE_ID));
                        } else {
                            Utils.showMessage(response.getErrorMessage());
                        }
                    } catch (IOException | ClassNotFoundException exception) {
                        Utils.showMessage(exception.getMessage());
                    }
                } else {
                    Utils.showMessage("Node is already registered");
                }
            } else {
                Utils.showMessage("Please configured the distributor first");
            }
        } else {
            throw new InvalidCommandException();
        }
    }

}

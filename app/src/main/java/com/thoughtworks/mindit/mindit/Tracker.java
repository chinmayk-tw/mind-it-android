package com.thoughtworks.mindit.mindit;

import android.content.Context;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;
import com.thoughtworks.mindit.mindit.presenter.Presenter;

import java.util.HashMap;
import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.ResultListener;

public class Tracker implements MeteorCallback{
    private Meteor meteor;
    private String rootId;

    public Tree getTree() {
        return tree;
    }

    private Tree tree;

    public Tracker(Context context, String rootId) {
        this.rootId = rootId;
        Meteor.setLoggingEnabled(true);
        meteor = new Meteor(context, "ws://10.12.23.82:3000/websocket");
        meteor.setCallback(this);
    }

    public void subscribe(String rootId) {
        meteor.subscribe("mindmap", new String[]{rootId});
    }

    public boolean isConnected() {
        return meteor.isConnected();
    }

    public void findTree(String rootId) {
        meteor.call("findTree", new String[]{rootId}, new ResultListener() {
            @Override
            public void onSuccess(String jsonResponse) {
                // This is where tree will be initialized
                tree = JsonParserService.parse(jsonResponse);
            }

            @Override
            public void onError(String s, String s1, String s2) {

            }
        });
    }

    public boolean isNull(){
        return tree==null;
    }

    @Override
    public void onConnect(boolean b) {
        System.out.println("Connected");
        this.findTree(rootId);
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onDataAdded(String collectionName, String documentID, String fieldsJson) {
        System.out.println("Data added to <" + collectionName + "> in document <" + documentID + ">");
        System.out.println("    Added: " + fieldsJson);
        Node node = JsonParserService.parseNode(fieldsJson);
        node.set_id(documentID);

        if(!tree.isNodeExists(node)) {
            Node parent = tree.getNode(node.getParentId());
            if (parent == null)
                node.setDepth(0);
            else
                node.setDepth(parent.getDepth() + 1);
            tree.addNode(node, node.getIndex());
        }
    }

    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        System.out.println("Data Changed to <"+collectionName+"> in document <"+documentID+">");
        System.out.println("    Chnaged: " + updatedValuesJson);
        System.out.println("s3: " + removedValuesJson);
        Node node = tree.getNode(documentID);
    }

    @Override
    public void onDataRemoved(String collectionName, String documentID) {
        System.out.println("Data removed to <"+collectionName+"> in document <"+documentID+">");
    }

    @Override
    public void onException(Exception e) {

    }
}

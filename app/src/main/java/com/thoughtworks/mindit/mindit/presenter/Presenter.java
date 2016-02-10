package com.thoughtworks.mindit.mindit.presenter;

import com.thoughtworks.mindit.mindit.PublishSubscribe.IObserver;
import com.thoughtworks.mindit.mindit.Tracker;
import com.thoughtworks.mindit.mindit.constant.Constants;
import com.thoughtworks.mindit.mindit.constant.Fields;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;
import com.thoughtworks.mindit.mindit.view.IMindmapView;
import com.thoughtworks.mindit.mindit.view.model.UINode;

import java.util.ArrayList;
import java.util.HashMap;


public class Presenter implements IObserver {
    private Tracker tracker;
    private HashMap<String, UINode> nodeTree;
    private UINode uiNode;
    private IMindmapView mView;

    public Presenter(IMindmapView mView) {
        this.mView = mView;
        nodeTree = new HashMap<String, UINode>();
        tracker = Tracker.getInstance();
        tracker.registerThisToTree(this);
        uiNode = null;
    }

    public Presenter(Tracker tracker) {
        nodeTree = new HashMap<String, UINode>();
        this.tracker = tracker;
        //    tree.register(this);
        uiNode = null;
    }


    public UINode convertModelNodeToUINode(Node node) {
        int depth = node.getDepth() * Constants.PADDING_FOR_DEPTH;
        UINode uiNode = new UINode(node.getName(), depth, node.getParentId());
        uiNode.setId(node.getId());
        nodeTree.put(node.getId(), uiNode);
        updateUIChildSubtree(node, uiNode);
        return uiNode;
    }

    public Node convertUINodeToModelNode(UINode uiNode, Node parent) {
        Node node;
        String rootId;
        if (parent != null) {
            rootId = parent.getRootId();
            if (parent.isARoot())
                rootId = parent.getId();
            node = new Node(uiNode.getId(), uiNode.getName(), parent, rootId, parent.getChildSubTree().size());
        } else
            node = new Node(uiNode.getId(), uiNode.getName(), parent, null, 0);

        //----update child subtree---//
        for (int i = 0; i < uiNode.getChildSubTree().size(); i++) {
            node.getChildSubTree().add(i, uiNode.getChildSubTree().get(i).getId());
        }
        return node;
    }

    public ArrayList<UINode> buildNodeListFromTree() {

        UINode rootNode = convertModelNodeToUINode(this.tracker.getTree().getRoot());
        ArrayList<UINode> nodeList = new ArrayList<UINode>();
        if (nodeList.size() != 0)
            nodeList.clear();
        //---get expanded tree for the first time---//
        if (rootNode.getChildSubTree().size() != 0)
            rootNode.setStatus(Constants.STATUS.EXPAND.toString());
        else
            rootNode.setStatus(Constants.STATUS.COLLAPSE.toString());
        nodeList.add(0, rootNode);
        return nodeList;
    }

    public void updateUIChildSubtree(Node node, UINode uiNode) {
        ArrayList<String> keys = node.getChildSubTree();
        ArrayList<UINode> childSubTree = new ArrayList<UINode>();

        for (int i = 0; i < keys.size(); i++) {
            Node node1 = tracker.getTree().getNode(keys.get(i));
            int depth = node1.getDepth() * Constants.PADDING_FOR_DEPTH;
            UINode uiNode1 = new UINode(node1.getName(), depth, node.getId());
            uiNode1.setId(node1.getId());

            for (int j = 0; j < node1.getChildSubTree().size(); j++) {
                updateUIChildSubtree(node1, uiNode1);
            }

            childSubTree.add(i, uiNode1);
            nodeTree.put(uiNode1.getId(), uiNode1);
        }

        uiNode.setChildSubTree(childSubTree);
    }

    public void addNode(UINode uiNode) {
        Node parent = tracker.getTree().getNode(uiNode.getParentId());

        String rootId = parent.getRootId();
        if (parent.isARoot())
            rootId = parent.getId();
        Node node = new Node(Constants.EMPTY_STRING, uiNode.getName(), parent, rootId, 0);
        this.uiNode = uiNode;
        tracker.addChild(node);
    }

    public void updateNode(UINode uiNode) {
        Node node = tracker.getTree().getNode(uiNode.getId());
        node.setName(uiNode.getName());
        tracker.updateNode(node);
    }

    public void deleteNode(UINode uiNode) {
        tracker.deleteNode(uiNode.getId());
    }

    private ArrayList<UINode> addNewNodeFromWebToParent(Node parent) {
        ArrayList<String> temp = parent.getChildSubTree();
        ArrayList<UINode> childSubTree = new ArrayList<UINode>();
        for (int i = 0; i < temp.size(); i++) {
            UINode uiNode = nodeTree.get(temp.get(i));
            childSubTree.add(uiNode);
        }

        return childSubTree;
    }

    @Override
    public void update(int updateOption, String updateParameter) {
        switch (updateOption) {
            case 1:
                if (uiNode == null) {
                    uiNode = convertModelNodeToUINode(tracker.getTree().getLastUpdatedNode());
                } else {
                    this.uiNode.setId(tracker.getTree().getLastUpdatedNode().getId());
                    nodeTree.put(uiNode.getId(), uiNode);
                }
                this.uiNode = null;
                break;
            case 2:
                UINode lastUpdatedNode = nodeTree.get(tracker.getTree().getLastUpdatedNode().getId());
                switch (updateParameter) {
                    case Fields.NAME:
                        UINode tempUINode = lastUpdatedNode;
                        tempUINode.setName(tracker.getTree().getLastUpdatedNode().getName());
                        break;
                    case Fields.CHILD_SUBTREE:
                        UINode existingParent = lastUpdatedNode;
                        existingParent.setChildSubTree(this.addNewNodeFromWebToParent(tracker.getTree().getLastUpdatedNode()));
                        mView.updateChildTree(existingParent);
                        break;
                    case Fields.LEFT:
                    case Fields.RIGHT:
                        UINode root = lastUpdatedNode;
                        ArrayList<UINode> childSubTree = this.addNewNodeFromWebToParent(tracker.getTree().getLastUpdatedNode());
                        if (!(root.getChildSubTree().equals(childSubTree))) {
                            root.setChildSubTree(childSubTree);
                            mView.updateChildTree(root);
                        }
                        break;
                    case Fields.PARENT_ID:
                        Node node = tracker.getTree().getLastUpdatedNode();
                        UINode child = nodeTree.get(node.getId());
                        updateDepthOfAllChildrenInUINode(child, node.getDepth() * Constants.PADDING_FOR_DEPTH);
                        child.setDepth(node.getDepth() * Constants.PADDING_FOR_DEPTH);
                        break;
                }
                break;
            case 3:
                break;

        }

        mView.notifyDataChanged();

    }


    private void updateDepthOfAllChildrenInUINode(UINode node, int depth) {
        node.setDepth(depth);
        for (UINode child : node.getChildSubTree()) {
            updateDepthOfAllChildrenInUINode(child, depth + Constants.PADDING_FOR_DEPTH);
        }
    }

    public UINode getLeftfirstNode(){
        Tree tree=tracker.getTree();
        Node rootNode=tree.getRoot();
        UINode leftFirstNode=nodeTree.get(rootNode.getLeft().get(0));
        return leftFirstNode;
    }
}

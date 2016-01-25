package com.thoughtworks.mindit.mindit.view.adapter;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thoughtworks.mindit.mindit.Constants;
import com.thoughtworks.mindit.mindit.R;
import com.thoughtworks.mindit.mindit.view.model.UINode;

import java.util.ArrayList;

public class CustomAdapterHelper {
    private final CustomAdapter customAdapter;

    public CustomAdapterHelper(CustomAdapter customAdapter) {
        this.customAdapter = customAdapter;
    }

    void updateText(NodeHolder nodeHolder, UINode currentNode) {
        nodeHolder.textViewForName.setText(nodeHolder.editText.getText());
        currentNode.setName("" + nodeHolder.editText.getText());
        nodeHolder.editText.setVisibility(View.GONE);
        nodeHolder.textViewForName.setVisibility(View.VISIBLE);
        customAdapter.setNewNodePosition(-1);
    }

    void setText(final NodeHolder nodeHolder, View rowView, final UINode currentNode) {
        nodeHolder.textViewForName = (TextView) rowView.findViewById(R.id.name);
        nodeHolder.textViewForName.setText(currentNode.getName());


        nodeHolder.textViewForName.setHeight(customAdapter.getDeviceHeight() / Constants.HEIGHT_DIVIDER);

        editText(nodeHolder, currentNode);
    }

    void editText(final NodeHolder nodeHolder, final UINode currentNode) {
        nodeHolder.textViewForName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodeHolder.textViewForName.setVisibility(View.GONE);
                nodeHolder.editText.setVisibility(View.VISIBLE);
                nodeHolder.editText.requestFocus();
                nodeHolder.editText.setText(nodeHolder.textViewForName.getText());
                nodeHolder.editText.setSelection(nodeHolder.editText.getText().length());
                final InputMethodManager inputMethodManager = (InputMethodManager) customAdapter.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(nodeHolder.editText, InputMethodManager.SHOW_FORCED);
                }
                nodeHolder.editText.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            nodeHolder.textViewForName.setText(nodeHolder.editText.getText());
                            currentNode.setName("" + nodeHolder.editText.getText());
                            nodeHolder.editText.setVisibility(View.GONE);
                            nodeHolder.textViewForName.setVisibility(View.VISIBLE);
                            if (inputMethodManager != null) {
                                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            }
                            int position = customAdapter.getNodeArrayList().indexOf(currentNode);
                            customAdapter.getPresenter().updateChild(currentNode);
                            return true;
                        }
                        return false;
                    }
                });
            }
        });
    }

    void setEventToExpandCollpse(final int position, NodeHolder nodeHolder, final UINode currentNode) {
        nodeHolder.expandCollapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentNode.getChildSubTree().size() == 0)
                    return;
                if (currentNode.isExpanded()) {

                    collapse(position, currentNode);
                } else {
                    expand(position, currentNode);
                }
                currentNode.toggleStatus();

                customAdapter.notifyDataSetChanged();
            }
        });
    }

    void setEventToAddNode(final int position, NodeHolder nodeHolder, View rowView, final UINode currentNode) {
        nodeHolder.addNodeButton = (ImageView) rowView.findViewById(R.id.options);
        nodeHolder.addNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addChild(position, currentNode);
            }
        });
    }

    void addChild(int position, UINode parentNode) {


        if(parentNode.getStatus().equals("collapse"))
        {
            parentNode.setStatus("expand");
            expand(position,parentNode);
        }
        int i=position;
        while (++i < customAdapter.getNodeArrayList().size() && customAdapter.getNodeArrayList().get(i).getDepth() > parentNode.getDepth())
            ;


        customAdapter.setNewNodePosition(i);


        UINode node = new UINode("Enter Text", parentNode.getDepth() + 20, parentNode.getId());
        customAdapter.getNodeArrayList().add(i, node);

        parentNode.getChildSubTree().add(parentNode.getChildSubTree().size(), node);
        customAdapter.notifyDataSetChanged();
    }

    void addPadding(int position, View rowView) {
        RelativeLayout relativeLayout = (RelativeLayout) rowView.findViewById(R.id.layout);
        setPaddingForeNode(position, relativeLayout);
    }

    void setImageForExpandCollapse(NodeHolder nodeHolder, View rowView, UINode currentNode) {
        nodeHolder.expandCollapseButton = (ImageView) rowView.findViewById(R.id.expandCollapse);
        if (currentNode.getStatus().equalsIgnoreCase("expand")) {
            nodeHolder.expandCollapseButton.setImageResource(R.drawable.expand);
        } else {
            if (currentNode.getChildSubTree().size() == 0) {
                nodeHolder.expandCollapseButton.setImageResource(R.drawable.leaf);
            } else {
                nodeHolder.expandCollapseButton.setImageResource(R.drawable.collapse);
            }
        }

    }

    public void expand(int position, UINode currentNode) {
        int j = position + 1;
        ArrayList<UINode> nodes = currentNode.getChildSubTree();
        for (int i = 0; i < nodes.size(); i++) {
            customAdapter.getNodeArrayList().add(j++, nodes.get(i));
        }
    }

    void collapse(int position, UINode currentNode) {
        for (int i = position + 1; i < customAdapter.getNodeArrayList().size(); ) {
            if (customAdapter.getNodeArrayList().get(i).getDepth() > currentNode.getDepth()) {
                if (customAdapter.getNodeArrayList().get(i).getStatus().equals("expand"))
                    customAdapter.getNodeArrayList().get(i).toggleStatus();
                customAdapter.getNodeArrayList().remove(i);
            } else {

                break;
            }
        }
    }

    void setPaddingForeNode(int position, RelativeLayout relativeLayout) {
        relativeLayout.setPadding(customAdapter.getNodeArrayList().get(position).getDepth(), 0, 0, 0);
    }

    public void setNewNodePosition(int newNodePosition) {
        customAdapter.setNewNodePosition(newNodePosition);
    }

    public void addNewNode(int position, final NodeHolder nodeHolder, final UINode currentNode) {

        nodeHolder.textViewForName.setVisibility(View.GONE);
        nodeHolder.editText.setVisibility(View.VISIBLE);
        nodeHolder.editText.requestFocus();
        String s = "" + nodeHolder.textViewForName.getText();

        nodeHolder.editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    updateText(nodeHolder, currentNode);
                    customAdapter.getPresenter().addChild(currentNode);
                    return true;
                }
                return false;
            }
        });


    }
}
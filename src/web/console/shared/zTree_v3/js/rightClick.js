
		var remoteSetting = {
			async: {
				enable: false
			},
			edit: {
				enable: true,
				showRemoveBtn: false,
				showRenameBtn: false
			},
			data: {
				simpleData: {
					enable: true,
					idKey:'id',
					pIdKey:'pid',
					rootPId: 0
				}
			},
			check: {
				enable: true,
				chkStyle: "radio",
				radioType: "all"
			},
			callback : {
				onRightClick: OnRightClick
			}
		};
		function OnRightClick(event, treeId, treeNode) {
			if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
				remoteZTree.cancelSelectedNode();
				showRMenu("root", event.clientX, event.clientY);
			} else if (treeNode && !treeNode.noR) {
				remoteZTree.selectNode(treeNode);
				showRMenu("node", event.clientX, event.clientY);
			}
		}

		function showRMenu(type, x, y) {
			$("#rMenu ul").show();
			if (type=="root") {
				$("#m_del").hide();
				$("#m_check").hide();
				$("#m_unCheck").hide();
			} else {
				$("#m_del").show();
				$("#m_check").show();
				$("#m_unCheck").show();
			}

            y += document.body.scrollTop;
            x += document.body.scrollLeft;
            rMenu.css({"top":y+"px", "left":x+"px", "visibility":"visible"});

			$("body").bind("mousedown", onBodyMouseDown);
		}
		function hideRMenu() {
			if (rMenu) rMenu.css({"visibility": "hidden"});
			$("body").unbind("mousedown", onBodyMouseDown);
		}
		function onBodyMouseDown(event){
			if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
				rMenu.css({"visibility" : "hidden"});
			}
		}
		var addCount = 1;
		function addTreeNode(e) {
			hideRMenu();
			var newNode = { name:"新建文件夹" + (addCount++)};
			if (remoteZTree.getSelectedNodes()[0]) {
				  newNode.checked = false;
                newNode.isParent=true;
                newNode.id=remoteZTree.getSelectedNodes()[0].id+"/"+newNode.name;
				remoteZTree.addNodes(remoteZTree.getSelectedNodes()[0], newNode);
			} else {
                newNode.isParent=true;
                newNode.id=remoteZTree.getSelectedNodes()[0].id+"/"+newNode.name;
				remoteZTree.addNodes(null, newNode);
			}
		}
		function removeTreeNode() {
			hideRMenu();
			var nodes = remoteZTree.getSelectedNodes();
			if (nodes && nodes.length>0) {
				if (nodes[0].children && nodes[0].children.length > 0) {
					var msg = "要删除的节点是父节点，如果删除将连同子节点一起删掉。\n\n请确认！";
					if (confirm(msg)==true){
						remoteZTree.removeNode(nodes[0]);
					}
				} else {
					remoteZTree.removeNode(nodes[0]);
				}
			}
		}
		function checkTreeNode(checked) {
			var nodes = remoteZTree.getSelectedNodes();
			if (nodes && nodes.length>0) {
				remoteZTree.checkNode(nodes[0], checked, true);
			}
			hideRMenu();
		}
		function resetTree() {
			hideRMenu();
			$.fn.zTree.init($("#remoteTree"), remoteSetting, jsonObjectStr);
		}


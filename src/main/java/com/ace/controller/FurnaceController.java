package com.ace.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ace.domain.Recipe;
import com.ace.service.FurnaceService;

@Controller
public class FurnaceController {

	private static int v_r_idx = 0;
	private static int v_r_data_idx = 0;
	
	@Autowired
	private FurnaceService furnaceService;
	

	
	//레시피 화면
	@RequestMapping(value = "/furnace/recipe", method = RequestMethod.GET)
	public String recipe(Model model) {
		return "/furnace/recipeList.jsp";
	}
	
	//레시피 리스트
	@RequestMapping(value = "/furnace/recipe/recipeList", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> recipeList(Model model) {
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		List<Object> recipeList = new ArrayList<Object>();
		
		List<Recipe> getRecipeList = furnaceService.getRecipeList();
		
//		System.out.println(getRecipeList.size());
		
		for(int i=0; i<getRecipeList.size(); i++) {
			Map<String, Object> rowMap = new HashMap<String, Object>();
			rowMap.put("num", (i+1));
			rowMap.put("r_group", getRecipeList.get(i).getR_group());
			rowMap.put("r_name", getRecipeList.get(i).getR_name());
			rowMap.put("r_comment", getRecipeList.get(i).getR_comment());
			rowMap.put("r_createtime", getRecipeList.get(i).getR_createtime());
			rowMap.put("r_updatetime", getRecipeList.get(i).getR_updatetime());
			
			rowMap.put("r_idx", getRecipeList.get(i).getR_idx());
			rowMap.put("r_data_idx", getRecipeList.get(i).getR_data_idx());
			
			recipeList.add(rowMap);			
		}
		
		rtnMap.put("last_page",1);
		rtnMap.put("data", recipeList);
		
		return rtnMap;
	}
	
	//레시피 상세보기
	@RequestMapping(value = "/furnace/recipe/recipeData", method = RequestMethod.GET)
	public String recipeData(
			@RequestParam(required = false) int r_idx,
			@RequestParam(required = false) int r_data_idx) {
		
		v_r_idx = r_idx;
		v_r_data_idx = r_data_idx;
		
		System.out.println("v_r_idx : "+v_r_idx+"// v_r_data_idx : "+v_r_data_idx);
		
		return "/furnace/recipeData.jsp";
	}
	
	//레시피 상세보기 값 조회
	@RequestMapping(value = "/furnace/recipe/recipeDataList", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> recipeDataList(Model model){
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		List<Object> recipeList = new ArrayList<Object>();
		
		Recipe recipe = new Recipe();
		
		recipe.setR_idx(v_r_idx);
		recipe.setR_data_idx(v_r_data_idx);
		
		List<Recipe> recipeDataList = furnaceService.getRecipeDataList(recipe);
		
		for(int i=0; i<recipeDataList.size(); i++) {
			Map<String, Object> rowMap = new HashMap<String, Object>();
			
			rowMap.put("process-step-"+i,recipeDataList.get(i).getProcess_step());
			rowMap.put("time-"+i,recipeDataList.get(i).getTime());
			rowMap.put("temperature-"+i,recipeDataList.get(i).getTemper());
			
			recipeList.add(rowMap);
		}
		
		rtnMap.put("data",recipeList);
		
		//선택한 레시피의 기준값 초기화
		v_r_idx = 0;
		v_r_data_idx = 0;
		return rtnMap;
	}
	
	//레시피값 PLC 쓰기
	@RequestMapping(value = "/furnace/recipe/plcWrite", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, String> recipePlcWrite(@RequestBody List<NodeValuePair> nodeValuePairs)
	        throws UaException, InterruptedException, ExecutionException {
		
	    Map<String, String> response = new HashMap<String, String>();
	    try {
	
	        UShort namespaceIndex = Unsigned.ushort(2);
	        boolean allGood = true;
	
	        int chunkSize = 50;
	        for (int i = 0; i < nodeValuePairs.size(); i += chunkSize) {
	            int end = Math.min(nodeValuePairs.size(), i + chunkSize);
	            List<NodeValuePair> chunk = nodeValuePairs.subList(i, end);
	
	            List<CompletableFuture<StatusCode>> futures = new ArrayList<CompletableFuture<StatusCode>>();
	
	            for (NodeValuePair pair : chunk) {
	                String nodeIdStr = pair.getNodeId();
	                short valueStr = pair.getValue();
	
	                NodeId nodeId = new NodeId(namespaceIndex, nodeIdStr);
	                DataValue dataValue = new DataValue(new Variant(valueStr));
	
	                futures.add(MainController.client.writeValue(nodeId, dataValue));
	            }
	
	            for (CompletableFuture<StatusCode> future : futures) {
	                StatusCode statusCode = future.get();
	                if (!statusCode.isGood()) {
	                    allGood = false;
	                    System.out.println("Failed to write value: " + statusCode);
	                }
	            }
	        }
	
	        if (allGood) {
	            response.put("status", "success");
	            response.put("message", "All values written successfully");
	        } else {
	            response.put("status", "failure");
	            response.put("message", "Some values failed to write");
	        }
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
	
	    return response;
	}	
	
	//레시피 - 글로벌 파라미터 팝업창 열기
	@RequestMapping(value = "/furnace/recipe/globalParameter", method = RequestMethod.GET)
	public String globalParameter(Model model) {
		return "/furnace/globalParameter.jsp";
	}	
	
	
	public static class NodeValuePair {
	    private String nodeId;
	    private short value;
	
	    public String getNodeId() {
	        return nodeId;
	    }
	
	    public void setNodeId(String nodeId) {
	        this.nodeId = nodeId;
	    }
	
	    public short getValue() {
	        return value;
	    }
	
	    public void setValue(short value) {
	        this.value = value;
	    }
	}
	 
}

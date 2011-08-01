/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.tests.servlets.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.orion.internal.server.servlets.ProtocolConstants;
import org.eclipse.orion.server.git.GitConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class GitCheckoutTest extends GitTest {

	// modified + checkout = clean
	@Test
	public void testCheckoutAllPaths() throws Exception {
		// clone a repo
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), null);
		IPath clonePath = new Path("file").append(project.getString(ProtocolConstants.KEY_ID)).makeAbsolute();
		clone(clonePath);

		// get project metadata
		WebRequest request = getGetFilesRequest(project.getString(ProtocolConstants.KEY_CONTENT_LOCATION));
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		project = new JSONObject(response.getText());

		JSONObject testTxt = getChild(project, "test.txt");
		modifyFile(testTxt, "change");

		JSONObject folder1 = getChild(project, "folder");
		JSONObject folder1Txt = getChild(folder1, "folder.txt");
		modifyFile(folder1Txt, "change");

		JSONObject gitSection = project.getJSONObject(GitConstants.KEY_GIT);
		String gitStatusUri = gitSection.getString(GitConstants.KEY_STATUS);
		String gitCloneUri = GitStatusTest.getCloneUri(gitStatusUri);

		request = getCheckoutRequest(gitCloneUri, new String[] {"test.txt", "folder/folder.txt"});
		response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

		assertStatus(StatusResult.CLEAN, gitStatusUri);
	}

	// modified + checkout = clean
	@Test
	@Ignore("not supported yet")
	public void testCheckoutDotPath() throws Exception {
		// clone a repo
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), null);
		IPath clonePath = new Path("file").append(project.getString(ProtocolConstants.KEY_ID)).makeAbsolute();
		clone(clonePath);

		// get project metadata
		WebRequest request = getGetFilesRequest(project.getString(ProtocolConstants.KEY_CONTENT_LOCATION));
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		project = new JSONObject(response.getText());

		JSONObject testTxt = getChild(project, "test.txt");
		modifyFile(testTxt, "change");

		JSONObject folder1 = getChild(project, "folder");
		JSONObject folderTxt = getChild(folder1, "folder.txt");
		modifyFile(folderTxt, "change");

		JSONObject gitSection = project.getJSONObject(GitConstants.KEY_GIT);
		String gitStatusUri = gitSection.getString(GitConstants.KEY_STATUS);
		String gitCloneUri = GitStatusTest.getCloneUri(gitStatusUri);

		request = getCheckoutRequest(gitCloneUri, new String[] {"."});
		response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

		assertStatus(StatusResult.CLEAN, gitStatusUri);
	}

	// modified + checkout = clean
	@Test
	public void testCheckoutFolderPath() throws Exception {
		// clone a repo
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), null);
		IPath clonePath = new Path("file").append(project.getString(ProtocolConstants.KEY_ID)).makeAbsolute();
		clone(clonePath);

		// get project metadata
		WebRequest request = getGetFilesRequest(project.getString(ProtocolConstants.KEY_CONTENT_LOCATION));
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		project = new JSONObject(response.getText());

		JSONObject testTxt = getChild(project, "test.txt");
		modifyFile(testTxt, "change");

		JSONObject folder1 = getChild(project, "folder");
		JSONObject folderTxt = getChild(folder1, "folder.txt");
		modifyFile(folderTxt, "change");

		JSONObject gitSection = project.getJSONObject(GitConstants.KEY_GIT);
		String gitStatusUri = gitSection.getString(GitConstants.KEY_STATUS);
		String gitCloneUri = GitStatusTest.getCloneUri(gitStatusUri);

		request = getCheckoutRequest(gitCloneUri, new String[] {"folder"});
		response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

		// 'test.txt' is still modified
		assertStatus(new StatusResult().setModifiedNames("test.txt"), gitStatusUri);
	}

	@Test
	public void testCheckoutEmptyPaths() throws Exception {
		// clone a repo
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), null);
		IPath clonePath = new Path("file").append(project.getString(ProtocolConstants.KEY_ID)).makeAbsolute();
		clone(clonePath);

		// get project metadata
		WebRequest request = getGetFilesRequest(project.getString(ProtocolConstants.KEY_CONTENT_LOCATION));
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		project = new JSONObject(response.getText());

		JSONObject gitSection = project.optJSONObject(GitConstants.KEY_GIT);
		assertNotNull(gitSection);
		String gitCloneUri = gitSection.getString(GitConstants.KEY_CLONE);

		request = getCheckoutRequest(gitCloneUri, new String[] {});
		response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponseCode());
	}

	@Test
	public void testCheckoutPathInUri() throws Exception {
		// clone a repo
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), null);
		IPath clonePath = new Path("file").append(project.getString(ProtocolConstants.KEY_ID)).makeAbsolute();
		clone(clonePath);

		// get project metadata
		WebRequest request = getGetFilesRequest(project.getString(ProtocolConstants.KEY_CONTENT_LOCATION));
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		project = new JSONObject(response.getText());

		JSONObject testTxt = getChild(project, "test.txt");
		modifyFile(testTxt, "change");

		JSONObject gitSection = project.getJSONObject(GitConstants.KEY_GIT);
		String gitStatusUri = gitSection.getString(GitConstants.KEY_STATUS);
		String gitCloneUri = GitStatusTest.getCloneUri(gitStatusUri);

		// TODO: don't create URIs out of thin air
		request = getCheckoutRequest(gitCloneUri + "test.txt", new String[] {});
		response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponseCode());
	}

	@Test
	public void testCheckoutWrongPath() throws Exception {
		// clone a repo
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), null);
		IPath clonePath = new Path("file").append(project.getString(ProtocolConstants.KEY_ID)).makeAbsolute();
		clone(clonePath);

		// get project metadata
		WebRequest request = getGetFilesRequest(project.getString(ProtocolConstants.KEY_CONTENT_LOCATION));
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		project = new JSONObject(response.getText());

		JSONObject testTxt = getChild(project, "test.txt");
		modifyFile(testTxt, "change");

		JSONObject gitSection = project.getJSONObject(GitConstants.KEY_GIT);
		String gitStatusUri = gitSection.getString(GitConstants.KEY_STATUS);
		String gitCloneUri = GitStatusTest.getCloneUri(gitStatusUri);

		// 'notthere.txt' doesn't exist
		request = getCheckoutRequest(gitCloneUri, new String[] {"notthere.txt"});
		response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

		// nothing has changed
		assertStatus(new StatusResult().setModifiedNames("test.txt"), gitStatusUri);
	}

	// modified + checkout = clean
	@Test
	public void testCheckoutInFolder() throws Exception {
		// clone a repo
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), null);
		IPath clonePath = new Path("file").append(project.getString(ProtocolConstants.KEY_ID)).makeAbsolute();
		clone(clonePath);

		// get project metadata
		WebRequest request = getGetFilesRequest(project.getString(ProtocolConstants.KEY_CONTENT_LOCATION));
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		project = new JSONObject(response.getText());

		JSONObject testTxt = getChild(project, "test.txt");
		modifyFile(testTxt, "change");

		JSONObject folder1 = getChild(project, "folder");
		JSONObject folderTxt = getChild(folder1, "folder.txt");
		modifyFile(folderTxt, "change");

		JSONObject gitSection = folder1.getJSONObject(GitConstants.KEY_GIT);
		String gitStatusUri = gitSection.getString(GitConstants.KEY_STATUS);
		// we should get a proper clone URI here: /git/clone/file/{projectId}/
		String gitCloneUri = GitStatusTest.getCloneUri(gitStatusUri);

		request = getCheckoutRequest(gitCloneUri, new String[] {"folder/folder.txt"});
		response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

		// 'test.txt' is still modified
		assertStatus(new StatusResult().setModifiedNames("test.txt"), gitStatusUri);
	}

	@Test
	public void testCheckoutFileOutsideCurrentFolder() throws Exception {
		// see bug 347847
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject projectTop = createProjectOrLink(workspaceLocation, getMethodName() + "-top", null);
		IPath clonePathTop = new Path("file").append(projectTop.getString(ProtocolConstants.KEY_ID)).makeAbsolute();

		JSONObject projectFolder = createProjectOrLink(workspaceLocation, getMethodName() + "-folder", null);
		IPath clonePathFolder = new Path("file").append(projectFolder.getString(ProtocolConstants.KEY_ID)).append("folder").makeAbsolute();

		IPath[] clonePaths = new IPath[] {clonePathTop, clonePathFolder};

		for (IPath clonePath : clonePaths) {
			// clone a  repo
			JSONObject clone = clone(clonePath);
			String cloneContentLocation = clone.getString(ProtocolConstants.KEY_CONTENT_LOCATION);

			// get project/folder metadata
			WebRequest request = getGetFilesRequest(cloneContentLocation);
			WebResponse response = webConversation.getResponse(request);
			assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
			JSONObject folder = new JSONObject(response.getText());

			JSONObject testTxt = getChild(folder, "test.txt");
			modifyFile(testTxt, "change in file.txt");

			JSONObject folder1 = getChild(folder, "folder");
			JSONObject folderTxt = getChild(folder1, "folder.txt");
			modifyFile(folderTxt, "change folder/folder.txt");

			// check status
			JSONObject folder1GitSection = folder1.getJSONObject(GitConstants.KEY_GIT);
			String folder1GitStatusUri = folder1GitSection.getString(GitConstants.KEY_STATUS);
			String folder1GitCloneUri = GitStatusTest.getCloneUri(folder1GitStatusUri);

			request = GitStatusTest.getGetGitStatusRequest(folder1GitStatusUri);
			assertStatus(new StatusResult().setModifiedNames("folder/folder.txt", "test.txt").setModifiedPaths("folder.txt", "../test.txt"), folder1GitStatusUri);

			// use KEY_NAME not KEY_PATH
			// request = getCheckoutRequest(gitCloneUri, new String[] {testTxt.getString(ProtocolConstants.KEY_PATH)});
			request = getCheckoutRequest(folder1GitCloneUri, new String[] {testTxt.getString(ProtocolConstants.KEY_NAME)});
			response = webConversation.getResponse(request);
			assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

			// 'folder/folder.txt' is still modified
			assertStatus(new StatusResult().setModifiedNames("folder/folder.txt"), folder1GitStatusUri);
		}
	}

	@Test
	public void testCheckoutBranch() throws Exception {
		// clone a repo
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), null);
		IPath clonePath = new Path("file").append(project.getString(ProtocolConstants.KEY_ID)).makeAbsolute();
		JSONObject clone = clone(clonePath);
		String cloneContentLocation = clone.getString(ProtocolConstants.KEY_CONTENT_LOCATION);
		String cloneLocation = clone.getString(ProtocolConstants.KEY_LOCATION);
		String branchesLocation = clone.getString(GitConstants.KEY_BRANCH);

		// get project metadata
		WebRequest request = getGetFilesRequest(project.getString(ProtocolConstants.KEY_CONTENT_LOCATION));
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		project = new JSONObject(response.getText());

		// create branch 'a'
		branch(branchesLocation, "a");

		// checkout 'a'
		response = checkoutBranch(cloneLocation, "a");
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		Repository db1 = getRepositoryForContentLocation(cloneContentLocation);
		Git git = new Git(db1);
		GitRemoteTest.assertOnBranch(git, "a");
	}

	@Test
	public void testCheckoutEmptyBranchName() throws Exception {
		// clone a repo
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), null);
		IPath clonePath = new Path("file").append(project.getString(ProtocolConstants.KEY_ID)).makeAbsolute();
		JSONObject clone = clone(clonePath);
		String location = clone.getString(ProtocolConstants.KEY_LOCATION);

		// get project metadata
		WebRequest request = getGetFilesRequest(project.getString(ProtocolConstants.KEY_CONTENT_LOCATION));
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		project = new JSONObject(response.getText());

		// checkout
		response = checkoutBranch(location, "");
		assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getResponseCode());
	}

	@Test
	public void testCheckoutInvalidBranchName() throws Exception {
		// clone a repo
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject project = createProjectOrLink(workspaceLocation, getMethodName(), null);
		IPath clonePath = new Path("file").append(project.getString(ProtocolConstants.KEY_ID)).makeAbsolute();
		JSONObject clone = clone(clonePath);
		String location = clone.getString(ProtocolConstants.KEY_LOCATION);

		// get project metadata
		WebRequest request = getGetFilesRequest(project.getString(ProtocolConstants.KEY_CONTENT_LOCATION));
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		project = new JSONObject(response.getText());

		// checkout 'a', which hasn't been created
		response = checkoutBranch(location, "a");
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getResponseCode());
	}

	@Test
	public void testCheckoutAborted() throws Exception {
		URI workspaceLocation = createWorkspace(getMethodName());
		JSONObject projectTop = createProjectOrLink(workspaceLocation, getMethodName() + "-top", null);
		IPath clonePathTop = new Path("file").append(projectTop.getString(ProtocolConstants.KEY_ID)).makeAbsolute();

		JSONObject projectFolder = createProjectOrLink(workspaceLocation, getMethodName() + "-folder", null);
		IPath clonePathFolder = new Path("file").append(projectFolder.getString(ProtocolConstants.KEY_ID)).append("folder").makeAbsolute();

		IPath[] clonePaths = new IPath[] {clonePathTop, clonePathFolder};

		for (IPath clonePath : clonePaths) {
			// clone a  repo
			JSONObject clone = clone(clonePath);
			String cloneContentLocation = clone.getString(ProtocolConstants.KEY_CONTENT_LOCATION);

			// get project/folder metadata
			WebRequest request = getGetFilesRequest(cloneContentLocation);
			WebResponse response = webConversation.getResponse(request);
			assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
			JSONObject folder = new JSONObject(response.getText());

			JSONObject testTxt = getChild(folder, "test.txt");

			// create branch
			JSONObject testTxtGitSection = testTxt.getJSONObject(GitConstants.KEY_GIT);
			String cloneLocation = testTxtGitSection.getString(GitConstants.KEY_CLONE);
			request = getGetRequest(cloneLocation);
			response = webConversation.getResponse(request);
			assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
			JSONObject clones = new JSONObject(response.getText());
			JSONArray clonesArray = clones.getJSONArray(ProtocolConstants.KEY_CHILDREN);
			assertEquals(1, clonesArray.length());
			clone = clonesArray.getJSONObject(0);

			response = branch(clone.getString(GitConstants.KEY_BRANCH), "branch");

			// change on the branch
			modifyFile(testTxt, "master change");

			addFile(testTxt);

			commitFile(testTxt, "commit on master", false);

			// local change, not committed
			modifyFile(testTxt, "working tree change");

			// checkout
			response = checkoutBranch(clone.getString(ProtocolConstants.KEY_LOCATION), "branch");
			assertEquals(HttpURLConnection.HTTP_CONFLICT, response.getResponseCode());
			JSONObject result = new JSONObject(response.getText());
			assertEquals(HttpURLConnection.HTTP_CONFLICT, result.getInt("HttpCode"));
			assertEquals("Error", result.getString("Severity"));
			assertEquals("Checkout aborted.", result.getString("Message"));
			assertEquals("Checkout conflict with files: \ntest.txt", result.getString("DetailedMessage"));
		}
	}

	private WebRequest getCheckoutRequest(String location, String[] paths) throws IOException, JSONException {
		String requestURI;
		if (location.startsWith("http://")) {
			// assume the caller knows what he's doing
			// assertCloneUri(location);
			requestURI = location;
		} else {
			requestURI = SERVER_LOCATION + GIT_SERVLET_LOCATION + GitConstants.CLONE_RESOURCE + location;
		}
		JSONObject body = new JSONObject();
		JSONArray jsonPaths = new JSONArray();
		for (String path : paths)
			jsonPaths.put(path);
		body.put(ProtocolConstants.KEY_PATH, jsonPaths);
		WebRequest request = new PutMethodWebRequest(requestURI, getJsonAsStream(body.toString()), "UTF-8");
		request.setHeaderField(ProtocolConstants.HEADER_ORION_VERSION, "1");
		setAuthentication(request);
		return request;
	}
}

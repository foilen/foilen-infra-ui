/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.api.model.permission.LinkAction;
import com.foilen.infra.api.model.permission.PermissionLink;
import com.foilen.infra.api.model.permission.PermissionResource;
import com.foilen.infra.api.model.permission.ResourceAction;
import com.foilen.infra.api.model.permission.RoleCreateForm;
import com.foilen.infra.api.model.permission.RoleEditForm;
import com.foilen.infra.api.model.user.UserApiNewFormResult;
import com.foilen.infra.api.model.user.UserApiWithPagination;
import com.foilen.infra.api.model.user.UserRoleEditForm;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.infra.ui.test.mock.FakeDataServiceImpl;
import com.foilen.smalltools.test.asserts.AssertDiff;
import com.foilen.smalltools.test.asserts.AssertTools;

public class ApiUserPermissionsServiceImplTest extends AbstractSpringTests {

    @Autowired
    private ApiUserPermissionsService apiUserPermissionsService;

    public ApiUserPermissionsServiceImplTest() {
        super(true);
    }

    private UserApiWithPagination cleanup(UserApiWithPagination userApiWithPagination) {
        userApiWithPagination.getItems().forEach(it -> it.setCreatedOn(null));
        return userApiWithPagination;
    }

    @Test
    public void testOwnerRuleFindAll_admin_all() {
        AssertTools.assertJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testOwnerRuleFindAll_admin_all.json", getClass(),
                cleanupIds(apiUserPermissionsService.ownerRuleFindAll(FakeDataServiceImpl.API_USER_ID_ADMIN, 1)));
    }

    @Test
    public void testRoleAdd_admin() {
        List<?> initialRoles = findAllRoles();
        List<?> initialAudits = findAllAudits();

        RoleCreateForm form = new RoleCreateForm();
        form.setName("newRole");

        AssertTools.assertJsonComparisonWithoutNulls("Common-success.json", getClass(), cleanupErrors(apiUserPermissionsService.roleAdd(FakeDataServiceImpl.API_USER_ID_ADMIN, form)));

        AssertTools.assertDiffJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testRoleAdd_admin-diifRoles.json", getClass(), initialRoles, findAllRoles());
        AssertTools.assertDiffJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testRoleAdd_admin-diifAudits.json", getClass(), initialAudits, findAllAudits());
    }

    @Test
    public void testRoleAdd_user() {
        List<?> initialRoles = findAllRoles();
        List<?> initialAudits = findAllAudits();

        RoleCreateForm form = new RoleCreateForm();
        form.setName("newRole");

        AssertTools.assertJsonComparisonWithoutNulls("Common-ErrorGlobal-notAdmin.json", getClass(),
                cleanupErrors(apiUserPermissionsService.roleAdd(FakeDataServiceImpl.API_USER_ID_USER_ALPHA, form)));

        AssertTools.assertDiffJsonComparisonWithoutNulls(new AssertDiff(), initialRoles, findAllRoles());
        AssertTools.assertDiffJsonComparisonWithoutNulls(new AssertDiff(), initialAudits, findAllAudits());

    }

    @Test
    public void testRoleDelete_admin() {
        List<?> initialRoles = findAllRoles();
        List<?> initialAudits = findAllAudits();

        AssertTools.assertJsonComparisonWithoutNulls("Common-success.json", getClass(), cleanupErrors(apiUserPermissionsService.roleDelete(FakeDataServiceImpl.API_USER_ID_ADMIN, "alpha_admin")));

        AssertTools.assertDiffJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testRoleDelete_admin-diifRoles.json", getClass(), initialRoles, findAllRoles());
        AssertTools.assertDiffJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testRoleDelete_admin-diifAudits.json", getClass(), initialAudits, findAllAudits());
    }

    @Test
    public void testRoleDelete_user() {
        AssertTools.assertJsonComparisonWithoutNulls("Common-ErrorGlobal-notAdmin.json", getClass(),
                cleanupErrors(apiUserPermissionsService.roleDelete(FakeDataServiceImpl.API_USER_ID_USER_ALPHA, "alpha_admin")));
    }

    @Test
    public void testRoleEdit_admin() {
        List<?> initialRoles = findAllRoles();
        List<?> initialAudits = findAllAudits();

        RoleEditForm form = new RoleEditForm();
        form.getResources().add(new PermissionResource().setAction(ResourceAction.ALL));
        form.getResources().add(new PermissionResource().setAction(ResourceAction.ADD) //
                .setExplicitChange(true) //
                .setType("*") //
                .setOwner("alpha") //
        );
        form.getLinks().add(new PermissionLink().setAction(LinkAction.ALL));
        form.getLinks().add(new PermissionLink().setAction(LinkAction.DELETE) //
                .setExplicitChange(true) //
                .setFromType("*") //
                .setFromOwner("alpha") //
                .setLinkType(LinkTypeConstants.INSTALLED_ON) //
                .setToType(Machine.RESOURCE_TYPE) //
                .setToOwner("alpha") //
        );
        form.getLinks().add(new PermissionLink().setAction(LinkAction.ADD) //
                .setExplicitChange(true) //
                .setFromType(Application.RESOURCE_TYPE) //
                .setFromOwner("alpha") //
                .setLinkType(LinkTypeConstants.USES) //
                .setToType("") //
                .setToOwner(null) //
        );

        AssertTools.assertJsonComparisonWithoutNulls("Common-success.json", getClass(), cleanupErrors(apiUserPermissionsService.roleEdit(FakeDataServiceImpl.API_USER_ID_ADMIN, "alpha_admin", form)));

        AssertTools.assertDiffJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testRoleEdit_admin-diifRoles.json", getClass(), initialRoles, findAllRoles());
        AssertTools.assertDiffJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testRoleEdit_admin-diifAudits.json", getClass(), initialAudits, findAllAudits());
    }

    @Test
    public void testRoleFindAll_admin_all() {
        AssertTools.assertJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testRoleFindAll_admin_all.json", getClass(),
                apiUserPermissionsService.roleFindAll(FakeDataServiceImpl.API_USER_ID_ADMIN, 1, null));
    }

    @Test
    public void testRoleFindAll_admin_search() {
        AssertTools.assertJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testRoleFindAll_admin_search.json", getClass(),
                apiUserPermissionsService.roleFindAll(FakeDataServiceImpl.API_USER_ID_ADMIN, 1, "eta"));
    }

    @Test
    public void testRoleFindAll_user_all() {
        AssertTools.assertJsonComparisonWithoutNulls("Common-Error-notAdmin.json", getClass(),
                cleanupErrors(apiUserPermissionsService.roleFindAll(FakeDataServiceImpl.API_USER_ID_USER_ALPHA, 1, null)));
    }

    @Test
    public void testRoleFindOne_admin() {
        AssertTools.assertJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testRoleFindOne_admin.json", getClass(),
                apiUserPermissionsService.roleFindOne(FakeDataServiceImpl.API_USER_ID_ADMIN, "alpha_admin"));
    }

    @Test
    public void testRoleFindOne_admin_notExists() {
        AssertTools.assertJsonComparisonWithoutNulls("Common-Error-notExists.json", getClass(), cleanupErrors(apiUserPermissionsService.roleFindOne(FakeDataServiceImpl.API_USER_ID_ADMIN, "nope")));
    }

    @Test
    public void testRoleFindOne_user() {
        AssertTools.assertJsonComparisonWithoutNulls("Common-Error-notAdmin.json", getClass(),
                cleanupErrors(apiUserPermissionsService.roleFindOne(FakeDataServiceImpl.API_USER_ID_USER_ALPHA, "alpha_admin")));
    }

    @Test
    public void testUserApiAdminCreate_admin() {
        UserApiNewFormResult result = apiUserPermissionsService.userApiAdminCreate(FakeDataServiceImpl.API_USER_ID_ADMIN);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getUserId());
        Assert.assertNotNull(result.getPassword());
    }

    @Test
    public void testUserApiEdit_admin() {
        List<?> initialUserApis = findAllUserApis();
        List<?> initialAudits = findAllAudits();

        UserRoleEditForm form = new UserRoleEditForm();
        form.getRoles().add(FakeDataServiceImpl.ROLE_ALPHA_ADMIN);

        AssertTools.assertJsonComparisonWithoutNulls("Common-success.json", getClass(),
                cleanupErrors(apiUserPermissionsService.userApiEdit(FakeDataServiceImpl.API_USER_ID_ADMIN, FakeDataServiceImpl.API_USER_ID_USER_ALPHA, form)));

        AssertTools.assertDiffJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testUserApiEdit_admin-diifUserApis.json", getClass(), initialUserApis, findAllUserApis());
        AssertTools.assertDiffJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testUserApiEdit_admin-diifAudits.json", getClass(), initialAudits, findAllAudits());
    }

    @Test
    public void testUserApiFindAll_admin_all() {
        AssertTools.assertJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testUserApiFindAll_admin_all.json", getClass(),
                cleanup(apiUserPermissionsService.userApiFindAll(FakeDataServiceImpl.API_USER_ID_ADMIN, 1, null)));
    }

    @Test
    public void testUserHumanEdit_admin() {
        List<?> initialUserHumans = findAllUserHumans();
        List<?> initialAudits = findAllAudits();

        UserRoleEditForm form = new UserRoleEditForm();
        form.getRoles().add(FakeDataServiceImpl.ROLE_ALPHA_ADMIN);

        AssertTools.assertJsonComparisonWithoutNulls("Common-success.json", getClass(),
                cleanupErrors(apiUserPermissionsService.userHumanEdit(FakeDataServiceImpl.API_USER_ID_ADMIN, FakeDataServiceImpl.USER_ID_ALPHA, form)));

        AssertTools.assertDiffJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testUserHumanEdit_admin-diifUserHumans.json", getClass(), initialUserHumans, findAllUserHumans());
        AssertTools.assertDiffJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testUserHumanEdit_admin-diifAudits.json", getClass(), initialAudits, findAllAudits());
    }

    @Test
    public void testUserHumanFindAll_admin_all() {
        AssertTools.assertJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testUserHumanFindAll_admin_all.json", getClass(),
                apiUserPermissionsService.userHumanFindAll(FakeDataServiceImpl.API_USER_ID_ADMIN, 1, null));
    }

    @Test
    public void testUserHumanFindAll_admin_search_email_example() {
        AssertTools.assertJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testUserHumanFindAll_admin_search_email_example.json", getClass(),
                apiUserPermissionsService.userHumanFindAll(FakeDataServiceImpl.API_USER_ID_ADMIN, 1, "example"));
    }

    @Test
    public void testUserHumanFindAll_admin_search_email_example1() {
        AssertTools.assertJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testUserHumanFindAll_admin_search_email_example1.json", getClass(),
                apiUserPermissionsService.userHumanFindAll(FakeDataServiceImpl.API_USER_ID_ADMIN, 1, "example1"));
    }

    @Test
    public void testUserHumanFindAll_admin_search_userId() {
        AssertTools.assertJsonComparisonWithoutNulls("ApiUserPermissionsServiceImplTest-testUserHumanFindAll_admin_search_userId.json", getClass(),
                apiUserPermissionsService.userHumanFindAll(FakeDataServiceImpl.API_USER_ID_ADMIN, 1, "44"));
    }

    @Test
    public void testUserHumanFindAll_user() {
        AssertTools.assertJsonComparisonWithoutNulls("Common-Error-notAdmin.json", getClass(),
                cleanupErrors(apiUserPermissionsService.userHumanFindAll(FakeDataServiceImpl.API_USER_ID_USER_ALPHA, 1, null)));
    }

}

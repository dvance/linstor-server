package com.linbit.linstor.storage;

import java.util.HashMap;
import java.util.Map;

import com.linbit.linstor.api.ApiConsts;

/**
 *
 * @author Gabor Hernadi &lt;gabor.hernadi@linbit.com&gt;
 */
public class StorageConstants
{
    public static final String NAMESPACE_STOR_DRIVER = ApiConsts.NAMESPC_STORAGE_DRIVER;

    /*
     * LVM
     */
    public static final String CONFIG_LVM_VOLUME_GROUP_KEY = ApiConsts.KEY_STOR_POOL_VOLUME_GROUP;
    public static final String CONFIG_LVM_THIN_POOL_KEY = ApiConsts.KEY_STOR_POOL_THIN_POOL;

    public static final String CONFIG_LVM_CREATE_COMMAND_KEY = "lvmCreate";
    public static final String CONFIG_LVM_RESIZE_COMMAND_KEY = "lvmResize";
    public static final String CONFIG_LVM_REMOVE_COMMAND_KEY = "lvmRemove";
    public static final String CONFIG_LVM_CHANGE_COMMAND_KEY = "lvmChange";
    public static final String CONFIG_LVM_CONVERT_COMMAND_KEY = "lvmConvert";
    public static final String CONFIG_LVM_LVS_COMMAND_KEY = "lvmLvs";
    public static final String CONFIG_LVM_VGS_COMMAND_KEY = "lvmVgs";

    public static final String CONFIG_SIZE_ALIGN_TOLERANCE_KEY = "alignmentTolerance";

    /*
     * ZFS
     */
    public static final String CONFIG_ZFS_POOL_KEY = ApiConsts.KEY_STOR_POOL_ZPOOL;
    public static final String CONFIG_ZFS_THIN_POOL_KEY = ApiConsts.KEY_STOR_POOL_ZPOOLTHIN;
    public static final String CONFIG_ZFS_COMMAND_KEY = "zfs";

    /*
     * Swordfish
     */
    public static final String CONFIG_SF_URL_KEY = ApiConsts.KEY_STOR_POOL_SF_URL;
    public static final String CONFIG_SF_STOR_SVC_KEY = ApiConsts.KEY_STOR_POOL_SF_STOR_SVC;
    public static final String CONFIG_SF_STOR_POOL_KEY = ApiConsts.KEY_STOR_POOL_SF_STOR_POOL;
    public static final String CONFIG_SF_USER_NAME_KEY = ApiConsts.KEY_STOR_POOL_SF_USER_NAME;
    public static final String CONFIG_SF_USER_PW_KEY = ApiConsts.KEY_STOR_POOL_SF_USER_PW;
    public static final String CONFIG_SF_POLL_TIMEOUT_VLM_CRT_KEY = ApiConsts.KEY_STOR_POOL_SF_POLL_TIMEOUT_VLM_CRT;
    public static final String CONFIG_SF_POLL_RETRIES_VLM_CRT_KEY = ApiConsts.KEY_STOR_POOL_SF_POLL_RETRIES_VLM_CRT;
    public static final String CONFIG_SF_POLL_TIMEOUT_ATTACH_VLM_KEY = ApiConsts.KEY_STOR_POOL_SF_POLL_TIMEOUT_ATTACH_VLM;
    public static final String CONFIG_SF_POLL_RETRIES_ATTACH_VLM_KEY = ApiConsts.KEY_STOR_POOL_SF_POLL_RETRIES_ATTACH_VLM;
    public static final String CONFIG_SF_POLL_TIMEOUT_GREP_NVME_UUID_KEY = ApiConsts.KEY_STOR_POOL_SF_POLL_TIMEOUT_GREP_NVME_UUID;
    public static final String CONFIG_SF_POLL_RETRIES_GREP_NVME_UUID_KEY = ApiConsts.KEY_STOR_POOL_SF_POLL_RETRIES_GREP_NVME_UUID;
    public static final String CONFIG_SF_COMPOSED_NODE_NAME_KEY = ApiConsts.KEY_STOR_POOL_SF_COMPOSED_NODE_NAME;
    public static final String CONFIG_SF_RETRY_COUNT_KEY = ApiConsts.KEY_STOR_POOL_SF_RETRY_COUNT;
    public static final String CONFIG_SF_RETRY_DELAY_KEY = ApiConsts.KEY_STOR_POOL_SF_RETRY_DELAY;

    public static final Map<String, String> KEY_DESCRIPTION = new HashMap<>();
    public static final int CONFIG_SF_RETRY_COUNT_DEFAULT = 5;
    public static final long CONFIG_SF_RETRY_DELAY_DEFAULT = 2000L;

    static
    {
        KEY_DESCRIPTION.put(CONFIG_LVM_VOLUME_GROUP_KEY, "The volume group the driver should use");
        KEY_DESCRIPTION.put(CONFIG_LVM_CREATE_COMMAND_KEY, "Command to the 'lvcreate' executable");
        KEY_DESCRIPTION.put(CONFIG_LVM_RESIZE_COMMAND_KEY, "Command to the 'lvresize' executable");
        KEY_DESCRIPTION.put(CONFIG_LVM_REMOVE_COMMAND_KEY, "Command to the 'lvremove' executable");
        KEY_DESCRIPTION.put(CONFIG_LVM_CHANGE_COMMAND_KEY, "Command to the 'lvchange' executable");
        KEY_DESCRIPTION.put(CONFIG_LVM_LVS_COMMAND_KEY, "Command to the 'lvs' executable");
        KEY_DESCRIPTION.put(CONFIG_LVM_VGS_COMMAND_KEY, "Command to the 'vgs' executable");
        KEY_DESCRIPTION.put(
            CONFIG_SIZE_ALIGN_TOLERANCE_KEY,
            "Specifies how many times of the extent size the volume's size " +
            "can be larger than specified upon creation."
        );
    }

    private StorageConstants()
    {
    }
}

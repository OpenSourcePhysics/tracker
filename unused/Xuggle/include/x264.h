/*****************************************************************************
 * x264.h: h264 encoder library
 *****************************************************************************
 * Copyright (C) 2003-2008 x264 Project
 *
 * Authors: Laurent Aimar <fenrir@via.ecp.fr>
 *          Loren Merritt <lorenm@u.washington.edu>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02111, USA.
 *****************************************************************************/

#ifndef X264_X264_H
#define X264_X264_H

#if !defined(_STDINT_H) && !defined(_STDINT_H_) && \
    !defined(_INTTYPES_H) && !defined(_INTTYPES_H_)
# ifdef _MSC_VER
#  pragma message("You must include stdint.h or inttypes.h before x264.h")
# else
#  warning You must include stdint.h or inttypes.h before x264.h
# endif
#endif

#include <stdarg.h>

#define X264_BUILD 83

/* x264_t:
 *      opaque handler for encoder */
typedef struct x264_t x264_t;

/****************************************************************************
 * Initialisation structure and function.
 ****************************************************************************/
/* CPU flags
 */
#define X264_CPU_CACHELINE_32   0x000001  /* avoid memory loads that span the border between two cachelines */
#define X264_CPU_CACHELINE_64   0x000002  /* 32/64 is the size of a cacheline in bytes */
#define X264_CPU_ALTIVEC        0x000004
#define X264_CPU_MMX            0x000008
#define X264_CPU_MMXEXT         0x000010  /* MMX2 aka MMXEXT aka ISSE */
#define X264_CPU_SSE            0x000020
#define X264_CPU_SSE2           0x000040
#define X264_CPU_SSE2_IS_SLOW   0x000080  /* avoid most SSE2 functions on Athlon64 */
#define X264_CPU_SSE2_IS_FAST   0x000100  /* a few functions are only faster on Core2 and Phenom */
#define X264_CPU_SSE3           0x000200
#define X264_CPU_SSSE3          0x000400
#define X264_CPU_SHUFFLE_IS_FAST 0x000800 /* Penryn, Nehalem, and Phenom have fast shuffle units */
#define X264_CPU_STACK_MOD4     0x001000  /* if stack is only mod4 and not mod16 */
#define X264_CPU_SSE4           0x002000  /* SSE4.1 */
#define X264_CPU_SSE42          0x004000  /* SSE4.2 */
#define X264_CPU_SSE_MISALIGN   0x008000  /* Phenom support for misaligned SSE instruction arguments */
#define X264_CPU_LZCNT          0x010000  /* Phenom support for "leading zero count" instruction. */
#define X264_CPU_ARMV6          0x020000
#define X264_CPU_NEON           0x040000  /* ARM NEON */
#define X264_CPU_FAST_NEON_MRC  0x080000  /* Transfer from NEON to ARM register is fast (Cortex-A9) */

/* Analyse flags
 */
#define X264_ANALYSE_I4x4       0x0001  /* Analyse i4x4 */
#define X264_ANALYSE_I8x8       0x0002  /* Analyse i8x8 (requires 8x8 transform) */
#define X264_ANALYSE_PSUB16x16  0x0010  /* Analyse p16x8, p8x16 and p8x8 */
#define X264_ANALYSE_PSUB8x8    0x0020  /* Analyse p8x4, p4x8, p4x4 */
#define X264_ANALYSE_BSUB16x16  0x0100  /* Analyse b16x8, b8x16 and b8x8 */
#define X264_DIRECT_PRED_NONE        0
#define X264_DIRECT_PRED_SPATIAL     1
#define X264_DIRECT_PRED_TEMPORAL    2
#define X264_DIRECT_PRED_AUTO        3
#define X264_ME_DIA                  0
#define X264_ME_HEX                  1
#define X264_ME_UMH                  2
#define X264_ME_ESA                  3
#define X264_ME_TESA                 4
#define X264_CQM_FLAT                0
#define X264_CQM_JVT                 1
#define X264_CQM_CUSTOM              2
#define X264_RC_CQP                  0
#define X264_RC_CRF                  1
#define X264_RC_ABR                  2
#define X264_AQ_NONE                 0
#define X264_AQ_VARIANCE             1
#define X264_AQ_AUTOVARIANCE         2
#define X264_B_ADAPT_NONE            0
#define X264_B_ADAPT_FAST            1
#define X264_B_ADAPT_TRELLIS         2
#define X264_WEIGHTP_NONE            0
#define X264_WEIGHTP_BLIND           1
#define X264_WEIGHTP_SMART           2
#define X264_B_PYRAMID_NONE          0
#define X264_B_PYRAMID_STRICT        1
#define X264_B_PYRAMID_NORMAL        2

static const char * const x264_direct_pred_names[] = { "none", "spatial", "temporal", "auto", 0 };
static const char * const x264_motion_est_names[] = { "dia", "hex", "umh", "esa", "tesa", 0 };
static const char * const x264_b_pyramid_names[] = { "none", "strict", "normal", 0 };
static const char * const x264_overscan_names[] = { "undef", "show", "crop", 0 };
static const char * const x264_vidformat_names[] = { "component", "pal", "ntsc", "secam", "mac", "undef", 0 };
static const char * const x264_fullrange_names[] = { "off", "on", 0 };
static const char * const x264_colorprim_names[] = { "", "bt709", "undef", "", "bt470m", "bt470bg", "smpte170m", "smpte240m", "film", 0 };
static const char * const x264_transfer_names[] = { "", "bt709", "undef", "", "bt470m", "bt470bg", "smpte170m", "smpte240m", "linear", "log100", "log316", 0 };
static const char * const x264_colmatrix_names[] = { "GBR", "bt709", "undef", "", "fcc", "bt470bg", "smpte170m", "smpte240m", "YCgCo", 0 };

/* Colorspace type
 * legacy only; nothing other than I420 is really supported. */
#define X264_CSP_MASK           0x00ff  /* */
#define X264_CSP_NONE           0x0000  /* Invalid mode     */
#define X264_CSP_I420           0x0001  /* yuv 4:2:0 planar */
#define X264_CSP_I422           0x0002  /* yuv 4:2:2 planar */
#define X264_CSP_I444           0x0003  /* yuv 4:4:4 planar */
#define X264_CSP_YV12           0x0004  /* yuv 4:2:0 planar */
#define X264_CSP_YUYV           0x0005  /* yuv 4:2:2 packed */
#define X264_CSP_RGB            0x0006  /* rgb 24bits       */
#define X264_CSP_BGR            0x0007  /* bgr 24bits       */
#define X264_CSP_BGRA           0x0008  /* bgr 32bits       */
#define X264_CSP_MAX            0x0009  /* end of list */
#define X264_CSP_VFLIP          0x1000  /* */

/* Slice type */
#define X264_TYPE_AUTO          0x0000  /* Let x264 choose the right type */
#define X264_TYPE_IDR           0x0001
#define X264_TYPE_I             0x0002
#define X264_TYPE_P             0x0003
#define X264_TYPE_BREF          0x0004  /* Non-disposable B-frame */
#define X264_TYPE_B             0x0005
#define IS_X264_TYPE_I(x) ((x)==X264_TYPE_I || (x)==X264_TYPE_IDR)
#define IS_X264_TYPE_B(x) ((x)==X264_TYPE_B || (x)==X264_TYPE_BREF)

/* Log level */
#define X264_LOG_NONE          (-1)
#define X264_LOG_ERROR          0
#define X264_LOG_WARNING        1
#define X264_LOG_INFO           2
#define X264_LOG_DEBUG          3

/* Threading */
#define X264_THREADS_AUTO 0 /* Automatically select optimal number of threads */
#define X264_SYNC_LOOKAHEAD_AUTO (-1) /* Automatically select optimal lookahead thread buffer size */

/* Zones: override ratecontrol or other options for specific sections of the video.
 * See x264_encoder_reconfig() for which options can be changed.
 * If zones overlap, whichever comes later in the list takes precedence. */
typedef struct
{
    int i_start, i_end; /* range of frame numbers */
    int b_force_qp; /* whether to use qp vs bitrate factor */
    int i_qp;
    float f_bitrate_factor;
    struct x264_param_t *param;
} x264_zone_t;

typedef struct x264_param_t
{
    /* CPU flags */
    unsigned int cpu;
    int         i_threads;       /* encode multiple frames in parallel */
    int         b_sliced_threads;  /* Whether to use slice-based threading. */
    int         b_deterministic; /* whether to allow non-deterministic optimizations when threaded */
    int         i_sync_lookahead; /* threaded lookahead buffer */

    /* Video Properties */
    int         i_width;
    int         i_height;
    int         i_csp;  /* CSP of encoded bitstream, only i420 supported */
    int         i_level_idc;
    int         i_frame_total; /* number of frames to encode if known, else 0 */

    struct
    {
        /* they will be reduced to be 0 < x <= 65535 and prime */
        int         i_sar_height;
        int         i_sar_width;

        int         i_overscan;    /* 0=undef, 1=no overscan, 2=overscan */

        /* see h264 annex E for the values of the following */
        int         i_vidformat;
        int         b_fullrange;
        int         i_colorprim;
        int         i_transfer;
        int         i_colmatrix;
        int         i_chroma_loc;    /* both top & bottom */
    } vui;

    int         i_fps_num;
    int         i_fps_den;

    /* Bitstream parameters */
    int         i_frame_reference;  /* Maximum number of reference frames */
    int         i_keyint_max;       /* Force an IDR keyframe at this interval */
    int         i_keyint_min;       /* Scenecuts closer together than this are coded as I, not IDR. */
    int         i_scenecut_threshold; /* how aggressively to insert extra I frames */
    int         b_intra_refresh;    /* Whether or not to use periodic intra refresh instead of IDR frames. */

    int         i_bframe;   /* how many b-frame between 2 references pictures */
    int         i_bframe_adaptive;
    int         i_bframe_bias;
    int         i_bframe_pyramid;   /* Keep some B-frames as references: 0=off, 1=strict hierarchical, 2=normal */

    int         b_deblocking_filter;
    int         i_deblocking_filter_alphac0;    /* [-6, 6] -6 light filter, 6 strong */
    int         i_deblocking_filter_beta;       /* [-6, 6]  idem */

    int         b_cabac;
    int         i_cabac_init_idc;

    int         b_interlaced;
    int         b_constrained_intra;

    int         i_cqm_preset;
    char        *psz_cqm_file;      /* JM format */
    uint8_t     cqm_4iy[16];        /* used only if i_cqm_preset == X264_CQM_CUSTOM */
    uint8_t     cqm_4ic[16];
    uint8_t     cqm_4py[16];
    uint8_t     cqm_4pc[16];
    uint8_t     cqm_8iy[64];
    uint8_t     cqm_8py[64];

    /* Log */
    void        (*pf_log)( void *, int i_level, const char *psz, va_list );
    void        *p_log_private;
    int         i_log_level;
    int         b_visualize;
    char        *psz_dump_yuv;  /* filename for reconstructed frames */

    /* Encoder analyser parameters */
    struct
    {
        unsigned int intra;     /* intra partitions */
        unsigned int inter;     /* inter partitions */

        int          b_transform_8x8;
        int          i_weighted_pred; /* weighting for P-frames */
        int          b_weighted_bipred; /* implicit weighting for B-frames */
        int          i_direct_mv_pred; /* spatial vs temporal mv prediction */
        int          i_chroma_qp_offset;

        int          i_me_method; /* motion estimation algorithm to use (X264_ME_*) */
        int          i_me_range; /* integer pixel motion estimation search range (from predicted mv) */
        int          i_mv_range; /* maximum length of a mv (in pixels). -1 = auto, based on level */
        int          i_mv_range_thread; /* minimum space between threads. -1 = auto, based on number of threads. */
        int          i_subpel_refine; /* subpixel motion estimation quality */
        int          b_chroma_me; /* chroma ME for subpel and mode decision in P-frames */
        int          b_mixed_references; /* allow each mb partition in P-frames to have it's own reference number */
        int          i_trellis;  /* trellis RD quantization */
        int          b_fast_pskip; /* early SKIP detection on P-frames */
        int          b_dct_decimate; /* transform coefficient thresholding on P-frames */
        int          i_noise_reduction; /* adaptive pseudo-deadzone */
        float        f_psy_rd; /* Psy RD strength */
        float        f_psy_trellis; /* Psy trellis strength */
        int          b_psy; /* Toggle all psy optimizations */

        /* the deadzone size that will be used in luma quantization */
        int          i_luma_deadzone[2]; /* {inter, intra} */

        int          b_psnr;    /* compute and print PSNR stats */
        int          b_ssim;    /* compute and print SSIM stats */
    } analyse;

    /* Rate control parameters */
    struct
    {
        int         i_rc_method;    /* X264_RC_* */

        int         i_qp_constant;  /* 0-51 */
        int         i_qp_min;       /* min allowed QP value */
        int         i_qp_max;       /* max allowed QP value */
        int         i_qp_step;      /* max QP step between frames */

        int         i_bitrate;
        float       f_rf_constant;  /* 1pass VBR, nominal QP */
        float       f_rate_tolerance;
        int         i_vbv_max_bitrate;
        int         i_vbv_buffer_size;
        float       f_vbv_buffer_init; /* <=1: fraction of buffer_size. >1: kbit */
        float       f_ip_factor;
        float       f_pb_factor;

        int         i_aq_mode;      /* psy adaptive QP. (X264_AQ_*) */
        float       f_aq_strength;
        int         b_mb_tree;      /* Macroblock-tree ratecontrol. */
        int         i_lookahead;

        /* 2pass */
        int         b_stat_write;   /* Enable stat writing in psz_stat_out */
        char        *psz_stat_out;
        int         b_stat_read;    /* Read stat from psz_stat_in and use it */
        char        *psz_stat_in;

        /* 2pass params (same as ffmpeg ones) */
        float       f_qcompress;    /* 0.0 => cbr, 1.0 => constant qp */
        float       f_qblur;        /* temporally blur quants */
        float       f_complexity_blur; /* temporally blur complexity */
        x264_zone_t *zones;         /* ratecontrol overrides */
        int         i_zones;        /* number of zone_t's */
        char        *psz_zones;     /* alternate method of specifying zones */
    } rc;

    /* Muxing parameters */
    int b_aud;                  /* generate access unit delimiters */
    int b_repeat_headers;       /* put SPS/PPS before each keyframe */
    int b_annexb;               /* if set, place start codes (4 bytes) before NAL units,
                                 * otherwise place size (4 bytes) before NAL units. */
    int i_sps_id;               /* SPS and PPS id number */
    int b_vfr_input;            /* VFR input */
    int i_timebase_num;         /* Timebase numerator */
    int i_timebase_den;         /* Timebase denominator */

    /* Slicing parameters */
    int i_slice_max_size;    /* Max size per slice in bytes; includes estimated NAL overhead. */
    int i_slice_max_mbs;     /* Max number of MBs per slice; overrides i_slice_count. */
    int i_slice_count;       /* Number of slices per frame: forces rectangular slices. */

    /* Optional callback for freeing this x264_param_t when it is done being used.
     * Only used when the x264_param_t sits in memory for an indefinite period of time,
     * i.e. when an x264_param_t is passed to x264_t in an x264_picture_t or in zones.
     * Not used when x264_encoder_reconfig is called directly. */
    void (*param_free)( void* );
} x264_param_t;

typedef struct {
    int level_idc;
    int mbps;        /* max macroblock processing rate (macroblocks/sec) */
    int frame_size;  /* max frame size (macroblocks) */
    int dpb;         /* max decoded picture buffer (bytes) */
    int bitrate;     /* max bitrate (kbit/sec) */
    int cpb;         /* max vbv buffer (kbit) */
    int mv_range;    /* max vertical mv component range (pixels) */
    int mvs_per_2mb; /* max mvs per 2 consecutive mbs. */
    int slice_rate;  /* ?? */
    int bipred8x8;   /* limit bipred to >=8x8 */
    int direct8x8;   /* limit b_direct to >=8x8 */
    int frame_only;  /* forbid interlacing */
} x264_level_t;

/* all of the levels defined in the standard, terminated by .level_idc=0 */
extern const x264_level_t x264_levels[];

/* x264_param_default:
 *      fill x264_param_t with default values and do CPU detection */
void    x264_param_default( x264_param_t * );

/* x264_param_parse:
 *  set one parameter by name.
 *  returns 0 on success, or returns one of the following errors.
 *  note: BAD_VALUE occurs only if it can't even parse the value,
 *  numerical range is not checked until x264_encoder_open() or
 *  x264_encoder_reconfig().
 *  value=NULL means "true" for boolean options, but is a BAD_VALUE for non-booleans. */
#define X264_PARAM_BAD_NAME  (-1)
#define X264_PARAM_BAD_VALUE (-2)
int x264_param_parse( x264_param_t *, const char *name, const char *value );

/****************************************************************************
 * Picture structures and functions.
 ****************************************************************************/
typedef struct
{
    int     i_csp;

    int     i_plane;
    int     i_stride[4];
    uint8_t *plane[4];
} x264_image_t;

typedef struct
{
    /* In: force picture type (if not auto)
     *     If x264 encoding parameters are violated in the forcing of picture types,
     *     x264 will correct the input picture type and log a warning.
     *     The quality of frametype decisions may suffer if a great deal of fine-grained
     *     mixing of auto and forced frametypes is done.
     * Out: type of the picture encoded */
    int     i_type;
    /* In: force quantizer for > 0 */
    int     i_qpplus1;
    /* Out: whether this frame is a keyframe.  Important when using modes that result in
     * SEI recovery points being used instead of IDR frames. */
    int     b_keyframe;
    /* In: user pts, Out: pts of encoded picture (user)*/
    int64_t i_pts;
    /* Out: frame dts. Since the pts of the first frame is always zero,
     *      initial frames may have a negative dts which must be dealt with by any muxer */
    int64_t i_dts;
    /* In: custom encoding parameters to be set from this frame forwards
           (in coded order, not display order). If NULL, continue using
           parameters from the previous frame.  Some parameters, such as
           aspect ratio, can only be changed per-GOP due to the limitations
           of H.264 itself; in this case, the caller must force an IDR frame
           if it needs the changed parameter to apply immediately. */
    x264_param_t *param;
    /* In: raw data */
    x264_image_t img;
    /* private user data. libx264 doesn't touch this,
       not even copy it from input to output frames. */
    void *opaque;
} x264_picture_t;

/* x264_picture_alloc:
 *  alloc data for a picture. You must call x264_picture_clean on it.
 *  returns 0 on success, or -1 on malloc failure. */
int x264_picture_alloc( x264_picture_t *pic, int i_csp, int i_width, int i_height );

/* x264_picture_clean:
 *  free associated resource for a x264_picture_t allocated with
 *  x264_picture_alloc ONLY */
void x264_picture_clean( x264_picture_t *pic );

/****************************************************************************
 * NAL structure and functions:
 ****************************************************************************/
/* nal */
enum nal_unit_type_e
{
    NAL_UNKNOWN = 0,
    NAL_SLICE   = 1,
    NAL_SLICE_DPA   = 2,
    NAL_SLICE_DPB   = 3,
    NAL_SLICE_DPC   = 4,
    NAL_SLICE_IDR   = 5,    /* ref_idc != 0 */
    NAL_SEI         = 6,    /* ref_idc == 0 */
    NAL_SPS         = 7,
    NAL_PPS         = 8,
    NAL_AUD         = 9,
    /* ref_idc == 0 for 6,9,10,11,12 */
};
enum nal_priority_e
{
    NAL_PRIORITY_DISPOSABLE = 0,
    NAL_PRIORITY_LOW        = 1,
    NAL_PRIORITY_HIGH       = 2,
    NAL_PRIORITY_HIGHEST    = 3,
};

/* The data within the payload is already NAL-encapsulated; the ref_idc and type
 * are merely in the struct for easy access by the calling application.
 * All data returned in an x264_nal_t, including the data in p_payload, is no longer
 * valid after the next call to x264_encoder_encode.  Thus it must be used or copied
 * before calling x264_encoder_encode or x264_encoder_headers again. */
typedef struct
{
    int i_ref_idc;  /* nal_priority_e */
    int i_type;     /* nal_unit_type_e */

    /* Size of payload in bytes. */
    int     i_payload;
    /* If param->b_annexb is set, Annex-B bytestream with 4-byte startcode.
     * Otherwise, startcode is replaced with a 4-byte size.
     * This size is the size used in mp4/similar muxing; it is equal to i_payload-4 */
    uint8_t *p_payload;
} x264_nal_t;

/****************************************************************************
 * Encoder functions:
 ****************************************************************************/

/* Force a link error in the case of linking against an incompatible API version.
 * Glue #defines exist to force correct macro expansion; the final output of the macro
 * is x264_encoder_open_##X264_BUILD (for purposes of dlopen). */
#define x264_encoder_glue1(x,y) x##y
#define x264_encoder_glue2(x,y) x264_encoder_glue1(x,y)
#define x264_encoder_open x264_encoder_glue2(x264_encoder_open_,X264_BUILD)

/* x264_encoder_open:
 *      create a new encoder handler, all parameters from x264_param_t are copied */
x264_t *x264_encoder_open( x264_param_t * );

/* x264_encoder_reconfig:
 *      analysis-related parameters from x264_param_t are copied.
 *      this takes effect immediately, on whichever frame is encoded next;
 *      due to delay, this may not be the next frame passed to encoder_encode.
 *      if the change should apply to some particular frame, use x264_picture_t->param instead.
 *      returns 0 on success, negative on parameter validation error. */
int     x264_encoder_reconfig( x264_t *, x264_param_t * );
/* x264_encoder_parameters:
 *      copies the current internal set of parameters to the pointer provided
 *      by the caller.  useful when the calling application needs to know
 *      how x264_encoder_open has changed the parameters, or the current state
 *      of the encoder after multiple x264_encoder_reconfig calls.
 *      note that the data accessible through pointers in the returned param struct
 *      (e.g. filenames) should not be modified by the calling application. */
void    x264_encoder_parameters( x264_t *, x264_param_t * );
/* x264_encoder_headers:
 *      return the SPS and PPS that will be used for the whole stream.
 *      if i_nal > 0, returns the total size of all NAL payloads.
 *      returns negative on error.
 *      the payloads of all output NALs are guaranteed to be sequential in memory. */
int     x264_encoder_headers( x264_t *, x264_nal_t **, int * );
/* x264_encoder_encode:
 *      encode one picture.
 *      if i_nal > 0, returns the total size of all NAL payloads.
 *      returns negative on error, zero if no NAL units returned.
 *      the payloads of all output NALs are guaranteed to be sequential in memory. */
int     x264_encoder_encode ( x264_t *, x264_nal_t **, int *, x264_picture_t *, x264_picture_t * );
/* x264_encoder_close:
 *      close an encoder handler */
void    x264_encoder_close  ( x264_t * );
/* x264_encoder_delayed_frames:
 *      return the number of currently delayed (buffered) frames
 *      this should be used at the end of the stream, to know when you have all the encoded frames. */
int     x264_encoder_delayed_frames( x264_t * );

#endif

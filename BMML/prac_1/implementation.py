import numpy as np
from scipy import einsum
from scipy.stats import binom, poisson
from scipy.signal import convolve
import warnings
import collections

# In variant 1 the following functions are required:

def pa(params, model=1):
    alen = params['amax'] - params['amin'] + 1
    val = np.arange(params['amin'], params['amax'] + 1)
    prob = np.ones(alen) / alen
    return (prob, val)
    
def pb(params, model=1):
    blen = params['bmax'] - params['bmin'] + 1
    val = np.arange(params['bmin'], params['bmax'] + 1)
    prob = np.ones(blen) / blen
    return (prob, val)

def pc(params, model=1):
    prob, val = pc_ab(np.arange(params['amin'], params['amax'] + 1),
                      np.arange(params['bmin'], params['bmax'] + 1),
                      params, model)
    prob = einsum('ijk->i', prob) / \
           (params['amax'] - params['amin'] + 1) / \
           (params['bmax'] - params['bmin'] + 1)
    prob[np.where(prob < 0)] = 0
    prob[np.isnan(prob)] = 0
    return (prob, val)

def pd(params, model=1):
    prob, val = pd_c(np.arange(params['amax'] + params['bmax'] + 1), params, model)
    cprob, _ = pc(params, model)
    prob = (prob @ np.diag(cprob)).sum(axis=1)
    prob[np.where(prob < 0)] = 0
    prob[np.isnan(prob)] = 0
    return (prob, val)
    
def pc_a(a, params, model=1):
    prob, val = pc_ab(a, np.arange(params['bmin'], params['bmax'] + 1), params, model)
    prob = prob.sum(axis=2) / (params['bmax'] - params['bmin'] + 1)
    prob[np.where(prob < 0)] = 0
    prob[np.isnan(prob)] = 0
    return (prob, val)

def pc_b(b, params, model=1):
    prob, val = pc_ab(np.arange(params['amin'], params['amax'] + 1), b, params, model)
    prob = prob.sum(axis=1) / (params['amax'] - params['amin'] + 1)
    prob[np.where(prob < 0)] = 0
    prob[np.isnan(prob)] = 0
    return (prob, val)
    
def pc_d(d, params, model=1):
    warnings.filterwarnings('ignore')
    if not isinstance(d, collections.Iterable):
        d = np.array([d])
    elif type(d) != np.ndarray:
        d = np.array(d)

    c_prob, val = pc(params, model)
    d_c_prob, _ = pd_c(val, params, model)
    d_c_prob = d_c_prob[d,:]
    d_prob, _ = pd(params, model)
    d_c_prob = d_c_prob * c_prob
    prob = d_c_prob / d_prob.reshape((-1,1))
    prob[np.isnan(prob)] = 0
    prob[np.where(prob < 0)] = 0
    prob = np.moveaxis(prob, (0, 1), (1, 0))
    bad = np.where(prob.sum(axis=0) == 0)[0]
    prob[:,bad] *= 0
    prob[-1, bad] = 1
    return (prob, val)

def pd_c(c, params, model=1):
    if not isinstance(c, collections.Iterable):
        c = np.array([c])
    elif type(c) != np.ndarray:
        c = np.array(c)

    dlen = 2 * (params['amax'] + params['bmax']) + 1

    val = np.arange(dlen)
    prob = binom.pmf(val[:, np.newaxis], c, params['p3'], loc=c)
    prob[np.where(prob < 0)] = 0
    prob[np.isnan(prob)] = 0
    return (prob, val)

def pc_ab(a, b, params, model=1):
    if not isinstance(a, collections.Iterable):
        a = np.array([a])
    elif type(a) != np.ndarray:
        a = np.array(a)

    if not isinstance(b, collections.Iterable):
        b = np.array([b])
    elif type(b) != np.ndarray:
        b = np.array(b)

    amin, amax = params['amin'], params['amax']
    bmin, bmax = params['bmin'], params['bmax']
    p1, p2, p3 = params['p1'], params['p2'], params['p3']
    alen = amax - amin + 1
    blen = bmax - bmin + 1
    clen = amax + bmax + 1
    val = np.arange(clen)
    prob = np.zeros((clen, alen, blen), dtype=np.float64)

    if (model == 1):
        aprobs = np.zeros((alen, clen), dtype=np.float64)
        bprobs = np.zeros((blen, clen), dtype=np.float64)

        for i in range(alen):
            aprobs[i] = binom.pmf(k=val, n=i + amin, p=p1).astype(np.float64)

        for i in range(blen):
            bprobs[i] = binom.pmf(k=val, n=i + bmin, p=p2).astype(np.float64)

        for i in range(alen):
            for j in range(blen):
                prob[:, i, j] = convolve(aprobs[i], bprobs[j])[:clen]

        for i in range(alen):
            for j in range(blen):
                prob[:, i, j] = convolve(aprobs[i], bprobs[j])[:clen]

    else:
        for i in range(alen):
            for j in range(blen):
                prob[:,i,j] = poisson.pmf(val, p1*(amin + i) + p2*(bmin + j))


    prob = prob[:, a - amin, :]
    prob = prob[:, :, b - bmin]
    prob[np.where(prob < 0)] = 0
    prob[np.isnan(prob)] = 0
    return (prob, val)
    
def pc_abd(a, b, d, params, model=1):
    warnings.filterwarnings('ignore')
    if not isinstance(a, collections.Iterable):
        a = np.array([a])
    elif type(a) != np.ndarray:
        a = np.array(a)

    if not isinstance(b, collections.Iterable):
        b = np.array([b])
    elif type(b) != np.ndarray:
        b = np.array(b)

    if not isinstance(d, collections.Iterable):
        d = np.array([d])
    elif type(d) != np.ndarray:
        d = np.array(d)

    c_ab_prob, val = pc_ab(a, b, params, model)
    d_c_prob, _ = pd_c(val, params, model)
    d_c_prob = d_c_prob[d,:].T
    prob = einsum('ijk,il->ijkl', c_ab_prob, d_c_prob, optimize='optimal', dtype=np.float64)
    prob /= prob.sum(axis=0)
    prob[np.where(prob < 0)] = 0
    prob[np.isnan(prob)] = 0
    return (prob, val)
import numpy as np
from scipy.stats import norm
from scipy.signal import fftconvolve

def calculate_log_probability(X, F, B, s):
    """
    Calculates log p(X_k|d_k,F,B,s) for all images X_k in X and
    all possible displacements d_k.

    Parameters
    ----------
    X : array, shape (H, W, K)
        K images of size H x W.
    F : array, shape (h, w)
        Estimate of villain's face.
    B : array, shape (H, W)
        Estimate of background.
    s : float
        Estimate of standard deviation of Gaussian noise.

    Returns
    -------
    ll : array, shape(H-h+1, W-w+1, K)
        ll[dh,dw,k] - log-likelihood of observing image X_k given
        that the villain's face F is located at displacement (dh, dw)
    """

    h, w = F.shape
    H, W, K = X.shape
    log_p = np.zeros((H-h+1, W-w+1, K), dtype=np.float64)

    for k in range(K):
        background = norm.logpdf(x=X[:, :, k], loc=B, scale=s)
        log_p[:, :, k] += 2 * fftconvolve(X[:, :, k], F[::-1, ::-1], mode='valid')
        log_p[:, :, k] -= (F ** 2).sum()
        log_p[:, :, k] -= fftconvolve((X[:, :, k] ** 2), np.ones(F.shape), mode='valid')
        log_p[:, :, k] /= (2 * (s ** 2))
        log_p[:, :, k] -= h * w * (np.log(s) + np.log(2 * np.pi) / 2)
        log_p[:, :, k] += (background.sum() - fftconvolve(background, np.ones(F.shape), mode='valid'))

    return log_p

def calculate_lower_bound(X, F, B, s, A, q, use_MAP=False):
    """
    Calculates the lower bound L(q,F,B,s,A) for the marginal log likelihood.

    Parameters
    ----------
    X : array, shape (H, W, K)
        K images of size H x W.
    F : array, shape (h, w)
        Estimate of villain's face.
    B : array, shape (H, W)
        Estimate of background.
    s : float
        Estimate of standard deviation of Gaussian noise.
    A : array, shape (H-h+1, W-w+1)
        Estimate of prior on displacement of face in any image.
    q : array
        If use_MAP = False: shape (H-h+1, W-w+1, K)
            q[dh,dw,k] - estimate of posterior of displacement (dh,dw)
            of villain's face given image Xk
        If use_MAP = True: shape (2, K)
            q[0,k] - MAP estimates of dh for X_k
            q[1,k] - MAP estimates of dw for X_k
    use_MAP : bool, optional
        If true then q is a MAP estimates of displacement (dh,dw) of
        villain's face given image Xk.

    Returns
    -------
    L : float
        The lower bound L(q,F,B,s,A) for the marginal log likelihood.
    """
    H, W, K = X.shape
    h, w = F.shape
    if use_MAP:
        new_q = np.zeros((H-h+1, W-w+1, K))
        for k in range(K):
            new_q[q[0,k], q[1,k]] = 1
        q = new_q

    log_p = calculate_log_probability(X, F, B, s)
    L = ((np.log((A+1e-64)[:,:,np.newaxis]) + log_p) * q).sum() - (np.log(q+1e-64) * q).sum()
    return L

def run_e_step(X, F, B, s, A, use_MAP=False):
    """
    Given the current esitmate of the parameters, for each image Xk
    esitmates the probability p(d_k|X_k,F,B,s,A).

    Parameters
    ----------
    X : array, shape(H, W, K)
        K images of size H x W.
    F  : array_like, shape(h, w)
        Estimate of villain's face.
    B : array shape(H, W)
        Estimate of background.
    s : scalar, shape(1, 1)
        Eestimate of standard deviation of Gaussian noise.
    A : array, shape(H-h+1, W-w+1)
        Estimate of prior on displacement of face in any image.
    use_MAP : bool, optional,
        If true then q is a MAP estimates of displacement (dh,dw) of
        villain's face given image Xk.

    Returns
    -------
    q : array
        If use_MAP = False: shape (H-h+1, W-w+1, K)
            q[dh,dw,k] - estimate of posterior of displacement (dh,dw)
            of villain's face given image Xk
        If use_MAP = True: shape (2, K)
            q[0,k] - MAP estimates of dh for X_k
            q[1,k] - MAP estimates of dw for X_k
    """
    H, W, K = X.shape
    #h, w = F.shape
    #q = np.zeros((H-h+1, W-w+1, K))

    #log_p = calculate_log_probability(X, F, B, s)
    #log_A = np.log(A+1e-64)
    #for k in range(K):
    #    log_q = log_A + log_p[:,:,k]
    #    log_q -= np.max(log_q)
    #    q[:,:,k] = np.exp(log_q) / np.sum(np.exp(log_q))

    log_p = calculate_log_probability(X, F, B, s)
    log_p_A = log_p + np.log(A+1e-64)[:,:,np.newaxis]
    log_p_A = log_p_A - log_p_A.max(axis=(0,1))[np.newaxis, np.newaxis,:]
    q = np.exp(log_p_A)
    q = q / (q.sum(axis=(0,1))[np.newaxis, np.newaxis,:])

    if use_MAP:
        new_q = np.zeros((2, K), dtype=int)
        for k in range(K):
            new_q[:,k] = np.unravel_index(np.argmax(q[:,:,k]), A.shape)
        q = new_q

    return q

def run_m_step(X, q, h, w, use_MAP=False):
    """
    Estimates F,B,s,A given esitmate of posteriors defined by q.

    Parameters
    ----------
    X : array, shape(H, W, K)
        K images of size H x W.
    q  :
        if use_MAP = False: array, shape (H-h+1, W-w+1, K)
           q[dh,dw,k] - estimate of posterior of displacement (dh,dw)
           of villain's face given image Xk
        if use_MAP = True: array, shape (2, K)
            q[0,k] - MAP estimates of dh for X_k
            q[1,k] - MAP estimates of dw for X_k
    h : int
        Face mask height.
    w : int
        Face mask width.
    use_MAP : bool, optional
        If true then q is a MAP estimates of displacement (dh,dw) of
        villain's face given image Xk.

    Returns
    -------
    F : array, shape (h, w)
        Estimate of villain's face.
    B : array, shape (H, W)
        Estimate of background.
    s : float
        Estimate of standard deviation of Gaussian noise.
    A : array, shape (H-h+1, W-w+1)
        Estimate of prior on displacement of face in any image.
    """
    H, W, K = X.shape
    if use_MAP:
        new_q = np.zeros((H-h+1, W-w+1, K))
        for k in range(K):
            new_q[q[0,k], q[1,k]] = 1
        q = new_q

    A = q.sum(axis=2) / K
    num = np.zeros((H, W))
    den = np.zeros((H, W))
    F = np.zeros((h, w))
    for k in range(K):
        F += fftconvolve(X[:, :, k], q[::-1,::-1,k], mode='valid')
        ql = (np.ones((H, W)) - fftconvolve(np.ones((h, w)), q[::-1,::-1,k], mode='full'))
        num += X[:,:,k] * ql
        den += ql

    F /= K
    B = np.zeros((H,W))
    no_zero = np.where(den != 0)
    B[no_zero] = num[no_zero] / den[no_zero]
    R = np.zeros((H-h+1, W-w+1, K))
    for k in range(K):
        R[:, :, k] -= 2 * fftconvolve(X[:, :, k], F[::-1, ::-1], mode='valid')
        R[:,:,k] += fftconvolve(2 * B * X[:, :, k] - B * B, np.ones((h,w)), mode='valid')
        R[:,:,k] += ((F ** 2).sum() + ((X[:,:,k] - B) ** 2).sum()) * np.ones((H-h+1, W-w+1))

    s = np.sqrt((R * q).sum() / (W * H * K))

    return F, B, s, A

def run_EM(X, h, w, F=None, B=None, s=None, A=None, tolerance=0.001,
           max_iter=50, use_MAP=False):
    """
    Runs EM loop until the likelihood of observing X given current
    estimate of parameters is idempotent as defined by a fixed
    tolerance.

    Parameters
    ----------
    X : array, shape (H, W, K)
        K images of size H x W.
    h : int
        Face mask height.
    w : int
        Face mask width.
    F : array, shape (h, w), optional
        Initial estimate of villain's face.
    B : array, shape (H, W), optional
        Initial estimate of background.
    s : float, optional
        Initial estimate of standard deviation of Gaussian noise.
    A : array, shape (H-h+1, W-w+1), optional
        Initial estimate of prior on displacement of face in any image.
    tolerance : float, optional
        Parameter for stopping criterion.
    max_iter  : int, optional
        Maximum number of iterations.
    use_MAP : bool, optional
        If true then after E-step we take only MAP estimates of displacement
        (dh,dw) of villain's face given image Xk.

    Returns
    -------
    F, B, s, A : trained parameters.
    LL : array, shape(number_of_iters + 2,)
        L(q,F,B,s,A) at initial guess, after each EM iteration and after
        final estimate of posteriors;
        number_of_iters is actual number of iterations that was done.
    """
    H, W, K = X.shape
    if F is None:
        F = np.random.rand(h, w)

    if B is None:
        B = np.random.rand(H, W)

    if A is None:
        A = np.ones((H-h+1, W-w+1)) / ((H-h+w) * (W-w+1))

    if s is None:
        s = np.random.random_sample()

    q = run_e_step(X, F, B, s, A, use_MAP)
    LL = []

    LL.append(calculate_lower_bound(X, F, B, s, A, q, use_MAP))

    for _ in np.arange(max_iter):
        q = run_e_step(X, F, B, s, A, use_MAP)
        F, B, s, A = run_m_step(X, q, h, w, use_MAP)
        LL.append(calculate_lower_bound(X, F, B, s, A, q, use_MAP))
        if (LL[-1] - LL[-2] < tolerance):
            break

    q = run_e_step(X, F, B, s, A, use_MAP)
    LL.append(calculate_lower_bound(X, F, B, s, A, q, use_MAP))
    return F, B, s, A, np.array(LL)


def run_EM_with_restarts(X, h, w, tolerance=0.001, max_iter=50, use_MAP=False,
                         n_restarts=10):
    """
    Restarts EM several times from different random initializations
    and stores the best estimate of the parameters as measured by
    the L(q,F,B,s,A).

    Parameters
    ----------
    X : array, shape (H, W, K)
        K images of size H x W.
    h : int
        Face mask height.
    w : int
        Face mask width.
    tolerance, max_iter, use_MAP : optional parameters for EM.
    n_restarts : int
        Number of EM runs.

    Returns
    -------
    F : array,  shape (h, w)
        The best estimate of villain's face.
    B : array, shape (H, W)
        The best estimate of background.
    s : float
        The best estimate of standard deviation of Gaussian noise.
    A : array, shape (H-h+1, W-w+1)
        The best estimate of prior on displacement of face in any image.
    L : float
        The best L(q,F,B,s,A).
    """
    F_best, B_best, s_best, A_best, LL = run_EM(X=X, h=h, w=w,
                                                tolerance=tolerance,
                                                max_iter=max_iter,
                                                use_MAP=use_MAP)
    L_best = LL[-1]
    for _ in range(n_restarts - 1):
        F, B, s, A, LL = run_EM(X=X, h=h, w=w, tolerance=tolerance,
                                max_iter=max_iter, use_MAP=use_MAP)
        L = LL[-1]
        if L > L_best:
            F_best = F
            B_best = B
            s_best = s
            A_best = A
            L_best = L

    return F_best, B_best, s_best, A_best, L_best
